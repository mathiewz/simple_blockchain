# simple_blockchain
A simple implementation of blokchain using java socket to communicate
The blocks can contain any Object implementing Serializable as data.

## Communication

All the connections are peer to peer connection using java sockets.
Each connection to another node aware of the blockchain will create a new Thread to both JVM connected.

## Usage

### create new node

To create a new Node, there are two possibilities : 

```java
// Create a new chain and is ready for new connections on port 8080
int listeningPort = 8080;
MyDataObject data = new MyDataObject();
Node<MyDataObject> node = new Node<>(listeningPort, new Block<>(data));
```
OR
```java
//Connect to 192.168.0.1:8080, get the current chain and is ready for new connections on port 8080
int listeningPort = 8080;
String remoteHost = "192.168.0.1";
int remotePort = 8080;
Node<MyDataObject> node = new Node<>(listeningPort, remoteHost, remotePort);
```

### Get the data of a block
```java
Block<MyDataObject> block = node.getBlockChain();
MyDataObject data = block.getData();
```

### Get latest block
```java
Block<MyDataObject> latest = node.getBlockChain();
```

### Add new Block to the chain
```java
MyDataObject data = new MyDataObject();
node.addBlock(data);
```

### Iterate through whole block chain

All of the next cases iterate through the block sorted by creation date

#### orEach
```java
for(Block<MyDataObject> block : node.getBlockChain()) {
        //Do some stuff
}
```
#### Iterator
```java
Iterator<Block<MyDataObject>> itr = node.getBlockChain().iterator();
while (itr.hasNext()) {
    Block<MyDataObject> block = itr.next();
    //Do some stuff            
}
```

#### Java 8 Stream API
```java
node.getBlockChain().stream().forEach(block -> {
   //Do somme stuff 
});
```

### Check the validity of a block
```java
Block<MyDataObject> block = node.getBlockChain();
block.isValid();
```

### Check the validity of the chain
```java
Block<MyDataObject> block = node.getBlockChain();
block.isWholeChainValid();
```
