[![Build Status](https://app.travis-ci.com/mtumilowicz/scala-zio2-grpc-workshop.svg?branch=master)](https://app.travis-ci.com/mtumilowicz/scala-zio2-grpc-workshop)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

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
    * https://github.com/grpc/grpc-java/issues/515#issuecomment-110023227
    * https://medium.com/rahasak/reactive-microservices-with-grpc-and-scala-e4767ca2d34a
    * [gRPC Cornerstone: HTTP2… or HTTP3? by Mykyta Protsenko & Alex Borysov](https://www.youtube.com/watch?v=rBQNGxUGhR0)

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
        * old code will read new messages without issues, ignoring any newly added fields
            * fields that were deleted will have their default value
            * deleted repeated fields will be empty
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
    * example - most popular ones
        ```
        option java_multiple_files = true;
        option java_package = "com.example.tutorial.protos";
        option java_outer_classname = "AddressBookProtos";
        ```
    * `java_package`
        * specifies in what Java package name your generated classes should live
        * default: matches the package name given by the package declaration
            * usually aren’t appropriate Java package names (they usually don’t start with a domain name)
        * even if specified - you should still define a normal package to avoid name collisions in the
        Protocol Buffers name space as well as in non-Java languages
    * `java_outer_classname`
        * defines the class name of the wrapper class which will represent this file
        * default: file name to upper camel case
            * example, `my_proto.proto` -> `MyProto`
    * `java_multiple_files = true`
        * enables generating a separate .java file for each generated class
        * legacy: generating a single .java file for the wrapper class, using the wrapper class
        as an outer class, and nesting all the other classes inside the wrapper class

## grpc
* gRPC = g Remote Procedure Call
* overview
    * client application can directly call a method on a server application on a different machine
    as if it were a local object
        * making easier to create distributed applications and services
    * based around the idea of defining a service with methods, parameters and return types
        * example
            ```
            service Greeter {
              rpc SayHello (HelloRequest) returns (HelloReply) {}
            }

            message HelloRequest {
              string name = 1;
            }

            message HelloReply {
              string message = 1;
            }
            ```
        * on the server side
            * server implements this interface and runs a gRPC server to handle client calls
                * gRPC infrastructure decodes incoming requests, executes service methods, and encodes service responses
        * on the client side
            * the client has a stub (referred to as just a client in some languages) that
            provides the same methods as the server
                * client can then just call those methods on the local object (stub)
                    * methods wrap the parameters for the call in the appropriate protocol buffer message type
                    send the requests to the server, and return the server’s protocol buffer responses
* gRPC library takes care of communication, marshalling, unmarshalling, and deadline enforcement
* uses HTTP/2
   * POST method
   * response status is always 200
* data is carried using data frames
   * we have `endStream` flag to know if current frame is the last frame of the message
   * we have `streamId` to differentiate between producers (usually frames are sent using the same channel)
* by default, gRPC uses Protocol Buffers
    * gRPC uses protocol buffer compiler protoc with a special gRPC plugin to generate code from your proto file
        * you get generated gRPC client and server code
        * as well as the regular protocol buffer code for populating, serializing, and retrieving your message types.
* four kinds of service method:
    * Unary
        * example
            ```
            rpc SayHello(HelloRequest) returns (HelloResponse);
            ```
        * just like a normal function call
            * client sends a single request to the server and gets a single response back
    * Server streaming
        * example
            ```
            rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse);
            ```
        * client sends a request to the server and gets a stream to read a sequence of messages back
            * client reads from the returned stream until there are no more messages
        * gRPC guarantees message ordering within an individual RPC call.
    * Client streaming
        * example
            ```
            rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse);
            ```
        * client writes a sequence of messages and sends them to the server (using a provided stream)
            * once the client has finished writing the messages, it waits for the server to read them and return its response
        * gRPC guarantees message ordering within an individual RPC call
    * Bidirectional streaming
        * example
            ```
            rpc BidiHello(stream HelloRequest) returns (stream HelloResponse);
            ```
        * both sides send a sequence of messages using a read-write stream
        * two streams operate independently
            * clients and servers can read and write in whatever order they like
        * order of messages in each stream is preserved.
* status
    * predefined - canonical status used by all gRPC servers and will only rarely be added to
        ```
        OK	        0	Not an error; returned on success.
        CANCELLED	1	The operation was cancelled, typically by the caller.
        // others
        ```
    * current set of codes should be able to appropriately describe almost any application status in a generic way
    * for custom status information, use Metadata
* both the client and server make independent and local determinations of the success of the call, and their conclusions may not match
    * RPC that finished successfully on the server side can fail on the client side
    * example: the server can send the response, but the reply can arrive at the client after their deadline has expired
* deadlines
    * are absolute timestamps that tell our system when the response of an RPC call is no longer needed
    * is sent to the server
        * computation is automatically interrupted when the deadline is exceeded
            * client call automatically ends with a `Status.DEADLINE_EXCEEDED` error
    * allow gRPC clients to specify how long they are willing to wait for an RPC to complete
        * when you don't specify a deadline, client requests never timeout
* Metadata
    * information about a particular RPC call (such as authentication details)
    * list of key-value pairs, where the keys are strings and the values are typically strings, but can be binary data

## ScalaPB
* is a protocol buffer compiler (protoc) plugin for Scala
* generate Scala case classes, parsers and serializers for your protocol buffers
* will look for protocol buffer (`.proto`) files under `src/main/protobuf`
    * configurable using the `Compile / PB.protoSources setting`
* running the compile command in sbt will generate Scala sources for your protos and compile them
* configuration
    * add `sbt-protoc` compiler plugin and ScalaPB Protobuf plugin dependencies to the `plugins.sbt` file
        ```
        addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

        libraryDependencies ++= Seq("com.thesamet.scalapb" %% "compilerplugin" % "0.11.13")
        ```
        * `sbt-protoc` plugin used `protoc` to generate code from Protobuf files
    * `build.sbt` needs to define the instructions to compile Protobuf definitions into Scala codes
        * compiles protobuf to scala code
        ```
            PB.targets in Compile := Seq(
              scalapb.gen() -> (sourceManaged in Compile).value
            )
        ```
        * libs
            ```
            libraryDependencies ++= {
              Seq(
                "io.grpc"                         % "grpc-netty"                      % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
                "com.trueaccord.scalapb"          %% "scalapb-runtime"                % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf",
              )
            }
            ```
## zio-grpc
* lets you write purely functional gRPC servers and clients
* supports all types of RPCs (unary, client streaming, server streaming, bidirectional)
* easily cancel RPCs by calling interrupt on the effect
    * server will immediately abort execution
* generates code into the same Scala package that ScalaPB uses
* easy request cancellations via fiber interrupts
* Context = Headers (Metadata)
* configuration
    * set up the ScalaPB code generator alongside the ZIO gRPC code generator
    * `plugins.sbt` = ScalaPB config plus `zio-grpc-codegen` lib
        ```
        libraryDependencies +=
          "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % version
        ```
    * `build.sbt` = ScalaPB config plus ZIO code gen step
        ```
        scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value / "scalapb"
        ```
* SBT plugin named `sbt-protoc` invokes two code generators
    * first: ScalaPB
        * generates case classes for all messages and some gRPC-related code that ZIO-gRPC interfaces with
    * second: ZIO gRPC code generator
        * generates a ZIO interface to your service
            * example: contains ZIO accessor methods that clients can use to talk to a RouteGuide server
* starting the server
    * `ServerMain` trait
        * used for simple applications
            * override the port (default is 9000)
            * override def services to return a ServiceList that contains our service
                ```
                ServiceList.addFromEnvironment[SomeService].provideLayer(SomeService.layer)
                ```
        * more control => take a look at the source code of `ServerMain` and customize
* instantiating a client
    * use `SomeClient.live(ZManagedChannel)` to create a `ZLayer` that can be used to provide a client
        * `ZManagedChannel` represent a virtual connection to a conceptual endpoint to perform RPCs
            * channel can have many actual connection to the endpoint
            * it is very common to have a single service client for each RPC service you need to connect to
