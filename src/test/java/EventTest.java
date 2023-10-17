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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import {{options.package}}.config.kafka.KafkaProcessor;
import {{options.package}}.domain.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class {{namePascalCase}}Test {

   private static final Logger LOGGER = LoggerFactory.getLogger(EventTest.class);
   
   @Autowired
   private KafkaProcessor processor;
   @Autowired
   private MessageCollector messageCollector;
   @Autowired
   private ApplicationContext applicationContext;

   {{#reaching "Aggregate" this}}
   @Autowired
   public {{pascalCase name}}Repository repository;
   {{/reaching}}

{{#examples}}
   @Test
   @SuppressWarnings("unchecked")
   public void test{{@index}}() {

      //given:  
   {{#reaching "Aggregate" ..}}
      {{pascalCase name}} entity = new {{pascalCase name}}();
   {{/reaching}}

   {{#given}}
   {{#each value}}
      entity.set{{pascalCase @key}}({{this}});
   {{/each}}
   {{/given}}

      repository.save(entity);

      //when:  
      
   {{#incoming "Event" ..}}
      {{pascalCase name}} event = new {{pascalCase name}}();
   {{/incoming}}

   {{#when}}
   {{#each value}}
      event.set{{pascalCase @key}}({{this}});
   {{/each}}
   {{/when}}
      
      InventoryApplication.applicationContext = applicationContext;

      ObjectMapper objectMapper = new ObjectMapper();
      try {
         String msg = objectMapper.writeValueAsString(event);

         processor.inboundTopic().send(
            MessageBuilder
            .withPayload(msg)
            .setHeader(
               MessageHeaders.CONTENT_TYPE,
               MimeTypeUtils.APPLICATION_JSON
            )
            .setHeader("type", event.getEventType())
            .build()
         );

         //then:

         Message<String> received = (Message<String>) messageCollector.forChannel(processor.outboundTopic()).poll();


      {{#outgoing "Event" ..}}
         {{pascalCase name}} outputEvent = objectMapper.readValue(received.getPayload(), {{pascalCase name}}.class);
      {{/outgoing}}

         LOGGER.info("Response received: {}", received.getPayload());

      {{#then}}
      {{#each value}}
         assertEquals(outputEvent.get{{pascalCase @key}}(), {{this}});
      {{/each}}
      {{/then}}


      } catch (JsonProcessingException e) {
         // TODO Auto-generated catch block
         assertTrue("exception", false);
      }

     
   }
{{/examples}}

}

