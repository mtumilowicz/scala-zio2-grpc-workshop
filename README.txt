
# scala-zio2-grpc-workshop

* references
    * https://github.com/scalapb/zio-grpc
    * https://zio.dev/ecosystem/community/zio-grpc/
    * https://scalapb.github.io/
    * https://medium.com/rahasak/asynchronous-microservices-with-zio-grpc-and-scala-2e6519cb4e9a
    * [Functional, Type-safe, Testable Microservices with ZIO gRPC](https://www.youtube.com/watch?v=XTkhxRTH1nE)
    * https://gitlab.com/rahasak-labs/zrpc
    * https://github.com/grpc/grpc-java/issues/515
    * https://grpc.io/docs/
    * https://protobuf.dev/overview/
    * https://scalac.io/blog/grpc-vs-rest-vs-graphql/
    * https://groups.google.com/g/protobuf/c/kF0IDYzcSsE?pli=1
    * https://stackoverflow.com/questions/69441101/is-it-a-good-practice-in-protobuf3-using-optional-to-check-nullability
    * https://cloud.google.com/apis/design/proto3
    * https://learn.microsoft.com/en-us/dotnet/architecture/grpc-for-wcf-developers/protobuf-reserved
    * https://codethecoffee.github.io/proto-cheatsheet/

## preface
* this project, for the sake of simplicity - has very basic setup
    * it is worth to take a look here: https://github.com/mtumilowicz/scala-zio2-test-sharing-resources-testcontainers-workshop
        * correct setup of multimodule scala project
* goals of these workshops
    * introduction into
        * protocol buffers
        * grpc
    * knowledge of scala ecosystem
        * zio-grpc
        * scalaPB
* workshop plan
    1. implement method to delete document
    1. show that streaming is working
        ```
        ZStream.from(DocumentApiOutput(...)).repeat(Schedule.spaced(2.seconds))
        ```

## protocol buffer
* are a combination of
    * the definition language (created in `.proto` files)
    * the code that the proto compiler generates to interface with data
    * language-specific runtime libraries
    * the serialization format for data that is written to a file (or sent across a network connection)
* are language-neutral, platform-neutral extensible mechanism for serializing structured data
* like JSON
    * but smaller, faster, and it generates native language bindings
* you define how you want your data to be structured
    * example
        ```
        message SearchRequest {
          string query = 1;
        }
        ```
    * then you can use special generated source code
        * to easily write and read your structured data to and from a variety of data streams
        * and using a variety of languages
* most often used for defining communications protocols (together with gRPC) and for data storage
* protocol buffers mechanics
    * ![alt text](img/overview.png)
* message fields types
    * `.proto` file
        ```
        message Example {
          string name = 1;
          optional string additionalDetails = 2;
          repeated int32 accounts = 3;
          map<string, int32> movieTicketPrice = 4;
        }
        ```
    * then after compilation
        ```
        Example(name = "A", additionalDetails = Some("1"), accounts = List(1), movieTicketPrice = Map("1" -> 10))
        ```
    * singular
        * default field rule
        * zero or one of this field (but not more than one)
        * singular just means: not "repeated"
    * optional
        * beginning in protobuf v3.14, primitive fields can distinguish between the default value and unset value
        by using the optional keyword, although this is generally discouraged
        * the same as singular
            * except that you can check to see if the value was explicitly set
        * entire point is to be able to distinguish between
            * no value was specified for field Foo
            * the field Foo was explicitly assigned the value that happens to be the proto3 default
            * without optional: the above both look identical (which is to say: the field is omitted)
                * if you don't need to distinguish between those two scenarios: you don't need optional
        * in one of two possible states:
            * set = contains a value that was explicitly set or parsed from the wire
                * it will be serialized to the wire
            * unset
                * will return the default value
                * will not be serialized to the wire
    * repeated
        * can be repeated zero or more times in a well-formed message
    * map
        * paired key/value field type
* enumerations
    ```
    enum CardinalDirection {
      WEST = 0;
    }

    message Move {
      CardinalDirection direction = 1;
    }
    ```
* other message types as field types
    ```
    message SearchResponse {
      repeated Result results = 1;
    }

    message Result {
      string url = 1;
      string title = 2;
      repeated string snippets = 3;
    }
    ```
    * if the message is defined in another proto file, you have to use import `"myproject/other_protos.proto";`
* updating messages
    * very simple to update message types without breaking any of your existing code
    * rule: don’t change the field numbers for any existing fields
    * fields can be removed, as long as the field number is not used again in your updated message type
        * problem: future users can reuse the field number when making their own updates to the type
            * can cause severe issues if they later load old versions of the same `.proto`
                * including data corruption, privacy bugs, and so on
        * advice
            * rename the field instead, perhaps adding the prefix “OBSOLETE_”
            * specify that the field numbers of your deleted fields are reserved
                ```
                message Stock {
                    reserved 3, 4;
                    int32 id = 1;
                    string symbol = 2;
                }
                ```
* packages
    * prevent name clashes between protocol message types
        * classes will be placed in a namespace of the same name
    * example
        ```
        package foo.bar;
        message Open { ... }
        ```
* options
    * example
        ```

        ```
    * Individual declarations in a .proto file can be annotated with a number of options.
    * Here are a few of the most commonly used options:
        * java_package (file option): The package you want to use for your generated Java/Kotlin classes. If no explicit java_package option is given in the .proto file, then by default the proto package (specified using the “package” keyword in the .proto file) will be used. However, proto packages generally do not make good Java packages since proto packages are not expected to start with reverse domain names. If not generating Java or Kotlin code, this option has no effect.

          option java_package = "com.example.foo";
        * java_outer_classname (file option): The class name (and hence the file name) for the wrapper Java class you want to generate. If no explicit java_outer_classname is specified in the .proto file, the class name will be constructed by converting the .proto file name to camel-case (so foo_bar.proto becomes FooBar.java). If the java_multiple_files option is disabled, then all other classes/enums/etc. generated for the .proto file will be generated within this outer wrapper Java class as nested classes/enums/etc. If not generating Java code, this option has no effect.

          option java_outer_classname = "Ponycopter";
        * java_multiple_files (file option): If false, only a single .java file will be generated for this .proto file, and all the Java classes/enums/etc. generated for the top-level messages, services, and enumerations will be nested inside of an outer class (see java_outer_classname). If true, separate .java files will be generated for each of the Java classes/enums/etc. generated for the top-level messages, services, and enumerations, and the wrapper Java class generated for this .proto file won’t contain any nested classes/enums/etc. This is a Boolean option which defaults to false. If not generating Java code, this option has no effect.

          option java_multiple_files = true;
* As long as you follow some simple practices when updating .proto definitions, old code will read new messages without issues, ignoring any newly added fields.
    * To the old code, fields that were deleted will have their default value, and deleted repeated fields will be empty.

## grpc
* https://github.com/grpc/grpc-java/issues/515#issuecomment-110023227
    * Status is for a canonical status used by all gRPC servers and will only rarely be added to. I think you saw the comment "If new codes are added over time they must choose a numerical value that does not collide with any previously used value." This comment is not referring to applications adding codes, but gRPC as a whole adding new codes. The current set of codes should be able to appropriately describe almost any application status in a generic way.

      For custom status information, use Metadata.
* gRPC uses HTTP/2 protocol which empowers its features such as: bidirectional binary communication, compression, flow control, compiled and strongly typed,  along with others.
* In gRPC, a client application can directly call a method on a server application on a different machine as if it were a local object, making it easier for you to create distributed applications and services.
    * As in many RPC systems, gRPC is based around the idea of defining a service, specifying the methods that can be called remotely with their parameters and return types. On the server side, the server implements this interface and runs a gRPC server to handle client calls. On the client side, the client has a stub (referred to as just a client in some languages) that provides the same methods as the server.
    * for example, you can easily create a gRPC server in Java with clients in Go, Python, or Ruby.
* By default, gRPC uses Protocol Buffers
    * The first step when working with protocol buffers is to define the structure for the data you want to serialize in a proto file: this is an ordinary text file with a .proto extension
        message Person {
          string name = 1;
          int32 id = 2;
          bool has_ponycopter = 3;
        }
    * Then, once you’ve specified your data structures, you use the protocol buffer compiler protoc to generate data access classes in your preferred language(s) from your proto definition.
    * You define gRPC services in ordinary proto files, with RPC method parameters and return types specified as protocol buffer messages:
        // The greeter service definition.
        service Greeter {
          // Sends a greeting
          rpc SayHello (HelloRequest) returns (HelloReply) {}
        }

        // The request message containing the user's name.
        message HelloRequest {
          string name = 1;
        }

        // The response message containing the greetings
        message HelloReply {
          string message = 1;
        }
    * gRPC uses protoc with a special gRPC plugin to generate code from your proto file: you get generated gRPC client and server code, as well as the regular protocol buffer code for populating, serializing, and retrieving your message types.
* gRPC lets you define four kinds of service method:
    * Unary RPCs where the client sends a single request to the server and gets a single response back, just like a normal function call.

      rpc SayHello(HelloRequest) returns (HelloResponse);
    * Server streaming RPCs where the client sends a request to the server and gets a stream to read a sequence of messages back. The client reads from the returned stream until there are no more messages. gRPC guarantees message ordering within an individual RPC call.

      rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse);
    * Client streaming RPCs where the client writes a sequence of messages and sends them to the server, again using a provided stream. Once the client has finished writing the messages, it waits for the server to read them and return its response. Again gRPC guarantees message ordering within an individual RPC call.

      rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse);
    * Bidirectional streaming RPCs where both sides send a sequence of messages using a read-write stream. The two streams operate independently, so clients and servers can read and write in whatever order they like: for example, the server could wait to receive all the client messages before writing its responses, or it could alternately read a message then write a message, or some other combination of reads and writes. The order of messages in each stream is preserved.

      rpc BidiHello(stream HelloRequest) returns (stream HelloResponse);
* Starting from a service definition in a .proto file, gRPC provides protocol buffer compiler plugins that generate client- and server-side code. gRPC users typically call these APIs on the client side and implement the corresponding API on the server side.
    * On the server side, the server implements the methods declared by the service and runs a gRPC server to handle client calls. The gRPC infrastructure decodes incoming requests, executes service methods, and encodes service responses.
    * On the client side, the client has a local object known as stub (for some languages, the preferred term is client) that implements the same methods as the service. The client can then just call those methods on the local object, and the methods wrap the parameters for the call in the appropriate protocol buffer message type, send the requests to the server, and return the server’s protocol buffer responses.
* In gRPC, both the client and server make independent and local determinations of the success of the call, and their conclusions may not match.
* Metadata
    * Metadata is information about a particular RPC call (such as authentication details) in the form of a list of key-value pairs, where the keys are strings and the values are typically strings, but can be binary data.

* in gRPC, both the client and server make their own independent and local determination about whether the remote procedure call (RPC) was successful. This means their conclusions may not match! An RPC that finished successfully on the server side can fail on the client side. For example, the server can send the response, but the reply can arrive at the client after their deadline has expired. The client will already have terminated with the status error DEADLINE_EXCEEDED. This should be checked for and managed at the application level.
* When you use gRPC, the gRPC library takes care of communication, marshalling, unmarshalling, and deadline enforcement.
* With gRPC we can define our service once in a .proto file and generate clients and servers in any of gRPC’s supported languages, which in turn can be run in environments ranging from servers inside a large data center to your own tablet — all the complexity of communication between different languages and environments is handled for you by gRPC.
* When you use a gRPC it is a very important to set deadlines.
    *  In gRPC, deadlines are absolute timestamps that tell our system when the response of an RPC call is no longer needed
    * The deadline is sent to the server, and the computation is automatically interrupted when the deadline is exceeded. The client call automatically ends with a Status.DEADLINE_EXCEEDED error.
    * When you don't specify a deadline, client requests never timeout
    * Deadlines allow gRPC clients to specify how long they are willing to wait for an RPC to complete before the RPC is terminated with the error DEADLINE_EXCEEDED
    * services should specify the longest default deadline they technically support, and clients should wait until the response is no longer useful to them

## zio-grpc
* ZIO-gRPC lets you write purely functional gRPC servers and clients
* Supports all types of RPCs (unary, client streaming, server streaming, bidirectional)
* Cancellable RPCs: easily cancel RPCs by calling interrupt on the effect. Server will immediately abort execution.
* ZIO gRPC generates code into the same Scala package that ScalaPB uses
    * Since java_package is specified, the Scala package will be the java_package with the proto file name appended to it
* When you compile the application in SBT (using compile), an SBT plugin named sbt-protoc invokes two code generators.
    * The first code generator is ScalaPB which generates case classes for all messages and some gRPC-related code that ZIO-gRPC interfaces with
    * The second generator is ZIO gRPC code generator, which generates a ZIO interface to your service.
        * example: contains ZIO accessor methods that clients can use to talk to a RouteGuide server
* Creating the server
    * There are two parts to making our RouteGuide service do its job:

      Implementing the trait ZRouteGuide generated from our service definition: returning the ZIO effects that do the actual "work" of our service.
      Putting an instance of ZRouteGuide behind a gRPC server to listen for requests from clients and return the service responses.
* Starting the server
  * RouteGuideServer extends ServerMain
  * ZIO gRPC provides a base trait to quickly set up gRPC services with zero boilerplate.

    We override the port we are going to use (default is 9000)
    Create an effect that constructs an instance of our service (we need an effectful construction since our service constructor takes a zio.Ref)
    Override def services to return a ServiceList that contains our service.
    * ServerMain is meant to be used for simple applications. If you need to do more in your initialization, you can take a look at the source code of ServerMain and customize
* Instantiating a client
    * Use RouteGuideClient.live to create a ZLayer that can be used to provide a client as a singleton to our program through the environment. In that case, throughout the program we use accessor methods, defined statically in RouteGuideClient that expect the client to be available in the environment.
    * A single ZManagedChannel represent a virtual connection to a conceptual endpoint to perform RPCs.
        * A channel can have many actual connection to the endpoint
        * Therefore, it is very common to have a single service client for each RPC service you need to connect to
* easy request cancellations via fiber interrupts
* Context = Headers (Metadata)
    * https://scalapb.github.io/zio-grpc/docs/next/context

## installation
* addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.2")

  libraryDependencies +=
    "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.5.0"
* Then, add the following lines to your build.sbt:
    PB.targets in Compile := Seq(
        scalapb.gen(grpc = true) -> (sourceManaged in Compile).value / "scalapb",
        scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value / "scalapb"
    )

    libraryDependencies ++= Seq(
        "io.grpc" % "grpc-netty" % "1.41.0",
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )
* This configuration will set up the ScalaPB code generator alongside the ZIO gRPC code generator.
    * Upon compilation, the source generator will process all proto files under src/main/protobuf
    * The ScalaPB generator will generate case classes for all messages as well as methods to serialize and deserialize those messages.
    * the ZIO gRPC code generator will generate code as described in the generated code section.
        * For each proto file that contains services definition, ZIO gRPC generates a Scala object that will contain service definitions for all services in that file.
        * The object name would be the proto file name prefixed with Zio
        * it would reside in the same Scala package that ScalaPB will use for definitions in that file.

## ScalaPB
* ScalaPB is a protocol buffer compiler (protoc) plugin for Scala. It will generate Scala case classes, parsers and serializers for your protocol buffers.
* To automatically generate Scala case classes for your messages add ScalaPB's sbt plugin to your project. Create a file named project/scalapb.sbt containing the following lines:

  addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.3")

  libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.11"
* Add the following line to your build.sbt:

  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
  )
  // (optional) If you need scalapb/scalapb.proto or anything from
  // google/protobuf/*.proto
  libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  )
* ScalaPB will look for protocol buffer (.proto) files under src/main/protobuf, which can be customized.
* running the compile command in sbt will generate Scala sources for your protos and compile them.
* The .proto file starts with a package declaration, which helps to prevent naming conflicts between different projects. In Scala, the package name followed by the file name is used as the Scala package unless you have either explicitly specified a java_package
* Each field must be annotated with one of the following modifiers:

  required: a value for the field must be provided when constructing a message case class. Parsing a message that misses a required field will throw an InvalidProtocolBufferException. Other than this, a required field behaves exactly like an optional field.
  optional: the field may or may not be set. If an optional field value isn't set, a default value is used. For simple types, you can specify your own default value, as we've done for the phone number type in the example. Otherwise, a system default is used: zero for numeric types, the empty string for strings, false for bools. For embedded messages, the default value is always the "default instance" or "prototype" of the message, which has none of its fields set. Calling the accessor to get the value of an optional (or required) field which has not been explicitly set always returns that field's default value. In proto2, optional fields are represented as Option[]. In proto3, optional primitives are not wrapped in Option[], but messages are.
  repeated: the field may be repeated any number of times (including zero). The order of the repeated values will be preserved in the protocol buffer. Think of repeated fields as dynamically sized arrays. They are represented in Scala as Seqs.
* The plugin assumes your proto files are under src/main/protobuf, however this is configurable using the Compile / PB.protoSources setting.
* gRPC Libraries for ScalaPB
    * libraryDependencies ++= Seq(
          "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
          "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
      )