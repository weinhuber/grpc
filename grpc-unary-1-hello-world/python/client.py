import grpc

import greeting_pb2
import greeting_pb2_grpc

def run():
   with grpc.insecure_channel('localhost:50051') as channel:
      stub = greeting_pb2_grpc.GreeterStub(channel)
      response = stub.greet(greeting_pb2.ClientInput(name='Python Version', greeting = "Yo"))
   print("Greeter client received following from server: " + response.message)

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    run()

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
