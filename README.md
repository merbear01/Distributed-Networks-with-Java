# Distributed-Networks-with-Java
The aim of the project is to implement a Command-Line
Interface (CLI) based networked distributed system for a group-based client-server communication,
which conforms with the following requirements. When a new member joins (connects), he/she may
provide the following parameters as an input (i.e., command-line parameters or parameters set via
the GUI):
1. an ID (please ensure that each member is assigned a unique ID),
2. a port and IP address of the server, and
3. its IP address and optionally a port it will listen to.
If a member is the first one in the group, the member must be informed about it at start-up. Then this
member will become the coordinator. Any new member will request details of existing members from
the server and will receive everyone's IDs, IP addresses and ports including the current group
coordinator. After this, the new member can contact everyone (through the server) to let them know
that they can update their set of members to include the new member. In case some of the members
do not respond, it will inform the server about this, and the server will inform other members to update
their local list of existing members. However, if the coordinator does not respond, then any new
member will become a coordinator. The coordinator maintains state of group members by checking
periodically how many of them are not responding and informs active members about it so that they
can update their state of members. Everyone should be able to send private or broadcast messages
to every other member through the server. The system should print out messages sent to/by the
members.
Importantly, your implementation can either run automatically or manually. In the former case, your
program may simulate the above task automatically without accepting any input parameters from
users while running. For instance, a client and server may exchange different messages periodically.
In the latter case, your program requires user input to simulate the task. For instance, a user will
type a message from a specific client to send it to other members or server. Any member can quit
by a simple ctrl-C command (CLI) 
