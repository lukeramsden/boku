1. Single threaded business logic

Maintaining synchronisation within business logic has a large performance and complexity overhead that is very rarely
worth it. It is much better to have single threaded units of business logic that communicate over in-memory, IPC or
network-based asynchronous communication mechanisms. This is the core of "Reactive Systems".

2. A test DSL

More on this
in [my blog post on the subject](https://lukeramsden.com/posts/testing-across-boundaries-with-internal-dsls/), but the
summary is that this allows us to separate test cases from the test implementation in a way that allows for much easier
evolution of the service interfaces and communication mechanisms.

3. Primarily integration tests

A good system is built with many layers of tests at lots of different boundaries - however, the highest impact win so
early on in a project is at a reasonably wide boundary (in this project they are called integration tests), as they
strike a good balance of quick to run (in-memory and does not require orchestrating other processes) while still having
things like serialisation and sockets included.