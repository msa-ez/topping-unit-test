forEach: Policy
fileName: {{namePascalCase}}Test.java
path: {{boundedContext.name}}/src/test/java/{{options.package}}
except: {{#checkExamples examples}}{{/checkExamples}}
---

package {{options.package}};

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import {{options.package}}.domain.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class {{namePascalCase}}Test {

   private static final Logger LOGGER = LoggerFactory.getLogger({{namePascalCase}}Test.class);
   
   @Autowired
   private MessageCollector messageCollector;
   @Autowired
   private ApplicationContext applicationContext;

   @Autowired
   private MessageVerifier<Message<?>> messageVerifier;

   {{#aggregateList}}
   @Autowired
   public {{namePascalCase}}Repository repository;
   {{/aggregateList}}

{{#examples}}
   @Test
   @SuppressWarnings("unchecked")
   public void test{{@index}}() {

      //given:
   {{#../aggregateList}}
   {{namePascalCase}} entity = new {{namePascalCase}}();
   {{/../aggregateList}}

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
   
   
   {{#../boundedContext}}
   {{namePascalCase}}Application.applicationContext = applicationContext;
   {{/../boundedContext}}

      ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      try {
         this.messageVerifier.send(MessageBuilder
                .withPayload(event)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader("type", event.getEventType())
                .build(), "{{../options.package}}");

         //then:
         {{^ifEquals then.[0].type "Aggregate"}}
         Message<?> receivedMessage = this.messageVerifier.receive("{{../options.package}}", 5000, TimeUnit.MILLISECONDS);
         assertNotNull("Resulted event must be published", receivedMessage);

         {{#outgoing "Event" ..}}
         String receivedPayload = (String) receivedMessage.getPayload();
         {{pascalCase name}} outputEvent = objectMapper.readValue(receivedPayload, {{pascalCase name}}.class);
         {{/outgoing}}


         LOGGER.info("Response received: {}", outputEvent);

         {{#then}}
         {{#each value}}
         assertEquals(outputEvent.get{{pascalCase @key}}(), {{{toJava this}}});
         {{/each}}
         {{/then}}
         {{/ifEquals}}

         {{#ifEquals then.[0].type "Aggregate"}}
         {{../aggregateList.[0].namePascalCase}} result = repository.findById(entity.get{{../aggregateList.[0].keyFieldDescriptor.namePascalCase}}()).get();

         LOGGER.info("Response received: {}", result);

         {{#then}}
         {{#each value}}
         assertEquals(result.get{{pascalCase @key}}(), {{{toJava this}}});
         {{/each}}
         {{/then}}
         {{/ifEquals}}

      } catch (JsonProcessingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         assertTrue(e.getMessage(), false);
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

window.$HandleBars.registerHelper('checkExamples', function (examples) {
  if (!examples) return false;

  function isAllNA(obj) {
     // Vue Observer 객체를 일반 객체로 변환
     obj = JSON.parse(JSON.stringify(obj));
     
     // null이나 undefined 체크
     if (obj === null || obj === undefined) {
        return true;
     }
     
     // 문자열인 경우
     if (typeof obj === 'string') {
        return obj === "N/A";  // N/A인 경우 true
     }
     
     // 숫자인 경우
     if (typeof obj === 'number') {
        return false;  // 숫자가 있으면 N/A가 아님
     }
     
     // 배열 검사
     if (Array.isArray(obj)) {
        return obj.every(item => isAllNA(item));  // 모든 요소가 N/A여야 true
     }
     
     // 객체 검사
     if (typeof obj === 'object') {
        return Object.values(obj).every(value => isAllNA(value));  // 모든 값이 N/A여야 true
     }
     
     return false;
  }

  // examples를 순수 객체로 변환
  examples = JSON.parse(JSON.stringify(examples));
  
  for (let example of examples) {
     let allNA = true;
     
     for (let key of ['given', 'when', 'then']) {
        if (example[key]?.[0]?.value) {
           if (!isAllNA(example[key][0].value)) {
              allNA = false;
              break;
           }
        }
     }
     
     if (!allNA) {
        return false;  // 하나라도 N/A가 아닌 값이 있으면 false
     }
  }
  
  return true;  // 모든 값이 N/A인 경우 true
});

</function>