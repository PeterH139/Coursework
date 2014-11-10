Distributed Systems Coursework 2014
Peter Henderson
-----

The structure of my simulator is as follows:

> My implementation is single threaded, and message passing is done in rounds. ie. all messages write to a buffer during
  their time step, and those messages are forwarded to the other nodes once every node in the system has executed a
  time step.

> A 'Network' object handles message passing between 'Node' objects. It also orchestrates the discovery, MST building,
  and data broadcast stages of the simulator.

> Node objects handle all messages in their message queue (FIFO) at each time step. This includes creating messages to
  send to some or all of its neighbours via the network.

> During discovery, the network will initialize nodes which will then send discover messages to all nodes in range. Upon
  receipt of a discover message a node will reply, and the node that receives this reply will add the node to its list
  of neighbours.

> Building the MST is implemented using the SynchGHS algorithm. At the step where nodes report to the leader that
  they have found a potential MWOE, a broadcast is used instead of a convergecast. This is as per the suggested
  simplification provided in the coursework handout.

My strategy for handling node deaths in the network is outlined below:

> After a node sends a data message to a neighbour, it checks to see if its energy level is below the minimum energy. If
  this is the case, then the node stops all data message sending and sends a "Node Down" message to its connected tree
  nodes. This node is now dead.

> When a node receives a "Node Down" message, it removes the message sender from its tree nodes, it declares itself the
  leader of this new detached tree, and it broadcasts an "Emergency Leader" message in its tree.

> When a node receives an "Emergency Leader" message, it sets its leader to the leaderId contained in the message and
  continues the broadcast in the tree.

> Once these steps have been carried out, the network will initiate building the MST again as before. This will repair
  the tree using negligible energy, ensuring that any future broadcasts cost as little as possible in this new MST.