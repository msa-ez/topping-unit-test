forEach: Policy
fileName: {{namePascalCase}}Test.java
path: {{boundedContext.name}}/src/test/java/{{options.package}}
---

package {{options.package}};

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;

import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import {{options.package}}.config.kafka.KafkaProcessor;
import {{options.package}}.domain.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMessageVerifier
public class {{namePascalCase}}Test {

   private static final Logger LOGGER = LoggerFactory.getLogger({{namePascalCase}}Test.class);
   
   @Autowired
   private KafkaProcessor processor;
   @Autowired
   private MessageCollector messageCollector;
   @Autowired
   private ApplicationContext applicationContext;

   @Autowired
   ObjectMapper objectMapper;

   @Autowired
   private MessageVerifier<Message<?>> messageVerifier;
   
   {{#outgoingCommandInfo}}
   {{#commandValue}}
   {{#aggregate}}
   @Autowired
   public {{namePascalCase}}Repository repository;
   {{/aggregate}}
   {{/commandValue}}
   {{/outgoingCommandInfo}}

{{#examples}}
   @Test
   @SuppressWarnings("unchecked")
   public void test{{@index}}() {

      //given:
   {{#../outgoingCommandInfo}}
   {{#commandValue}}
   {{#aggregate}}
   {{pascalCase name}} entity = new {{pascalCase name}}();
   {{/aggregate}}
   {{/commandValue}}
   {{/../outgoingCommandInfo}}

   {{#given}}
   {{#each value}}
      entity.set{{pascalCase @key}}({{{toJava this}}});
   {{/each}}
   {{/given}}

      repository.save(entity);

      //when:  
      
   {{#incoming "Event" ..}}
      {{pascalCase name}} event = new {{pascalCase name}}();
   {{/incoming}}

   {{#when}}
   {{#each value}}
      event.set{{pascalCase @key}}({{{toJava this}}});
   {{/each}}
   {{/when}}
   
   
   {{#../outgoingCommandInfo}}
   {{#commandValue}}
   {{#aggregate}}
   {{namePascalCase}}Application.applicationContext = applicationContext;
   {{/aggregate}}
   {{/commandValue}}
   {{/../outgoingCommandInfo}}

      ObjectMapper objectMapper = new ObjectMapper();
      try {
         String msg = objectMapper.writeValueAsString(event);

         this.messageVerifier.send(MessageBuilder
                .withPayload(msg)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader("type", event.getEventType())
                .build(), {{{toJava ../options.package}}});

         //then:

         Message<?> receivedMessage = this.messageVerifier.receive({{{toJava ../options.package}}}, 5000, TimeUnit.MILLISECONDS);

         assertNotNull("Resulted event must be published", receivedMessage);

      {{#outgoing "Event" ..}}
         {{pascalCase name}} outputEvent = objectMapper.readValue(receivedMessage.getPayload(), {{pascalCase name}}.class);
      {{/outgoing}}


         LOGGER.info("Response received: {}", receivedMessage.getPayload());

      {{#then}}
      {{#each value}}
         assertEquals(outputEvent.get{{pascalCase @key}}(), {{{toJava this}}});
      {{/each}}
      {{/then}}


      } catch (JsonProcessingException e) {
         // TODO Auto-generated catch block
         assertTrue("exception", false);
      }

     
   }
{{/examples}}

}

<function>

window.$HandleBars.registerHelper('toJava', convertToJavaSyntax)

function convertToJavaSyntax(value) {
  const type = typeof value;

  switch (type) {
    case 'string':
      return `"${value}"`; // Java에서 문자열은 큰따옴표를 사용합니다.
    case 'number':
      // JavaScript의 숫자는 정수 또는 부동소수점일 수 있으므로 이를 구분해야 할 수도 있습니다.
      if (Number.isSafeInteger(value)) {
        return `${value}L`; // long 타입으로 간주할 수 있습니다.
      } else {
        return `${value}D`; // double 타입으로 간주할 수 있습니다.
      }
    case 'boolean':
      return value.toString();
    case 'object':
      if (value instanceof Date) {
        return `new Date(${value.getTime()}L)`; // Java의 Date 생성자를 사용합니다.
      } else if (value === null) {
        return 'null';
      } else if (Array.isArray(value)) {
        // 배열의 경우 더 복잡한 로직이 필요할 수 있으며, 이는 예시로만 제공됩니다.
        const elements = value.map((element) => convertToJavaSyntax(element)).join(', ');
        return `new Object[]{${elements}}`; // Object 배열로 간주합니다.
      } else {
        // 다른 종류의 객체에 대한 처리가 필요할 수 있습니다.
        // 이 경우 해당 객체를 적절한 Java 표현으로 변환하는 로직이 필요합니다.
        return value.toString(); // 기본적인 toString 반환을 사용합니다.
      }
    default:
      throw new Error(`Unsupported type: ${type}`);
  }
}
