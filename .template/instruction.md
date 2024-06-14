#### Unit tests are tests conducted during the implementation phase to verify that each microservice functions as expected. 

#### This approach offers several benefits:

#### 1. Error Detection: Identifying and fixing bugs early in the development phase reduces the cost of fixing complex bugs later.

#### 2. Design Improvement: Writing unit tests helps developers better understand the code structure, leading to better design decisions.

#### 3. Facilitates Refactoring: Unit tests act as a safety net, ensuring that the code still works correctly after changes or refactoring.

#### 4. Increases Development Speed: In the long run, unit tests make the development process faster and more efficient by resolving issues early, preventing many potential problems later.

#### 5. Documentation: Unit tests can serve as documentation, showing how functionalities should be used. New developers can quickly understand the intent and usage of functionalities through unit tests.

#### Unit tests use the Given-When-Then pattern to ensure consistency and clarity in structuring tests, and can be set up as follows:

### How to Create Unit Tests

#### 1. In the modeling phase, create a Policy sticker and attach it to the Aggregate sticker.

#### 2. To create examples using the Given-When-Then pattern, set up Relations as Event - Policy - Event.

#### 3. Click 'Examples' on the Policy sticker panel and create examples following the Given-When-Then pattern.

### How to Run Unit Tests

```
cd <microservice> // Microservice with Generated Test Code
mvn test
```
