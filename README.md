# Play area for a content/data system

This is some some play around stuff and should by no means be
used by anybody to do anything.

If you want to get started, `IndexTest` is a good a place as any.

## Interesting Concepts

 * The base type is a key/field/value triple.  This is a specialization of
   RDF triples.  Sort of.  By coincidence, really.  See `class Update`

 * Everything is strung together by events.  Everybody listens to batches of
   those aforementioned triples.  See `BaseStorage#eventBus`

 * It's log based.  Everything centers around a write-once log.  Nothing is
   updated in place; modifications are just writing the new value to the 
   log. See `LogStorageSpi`

 * There is an in-memory object view of things, b/c that just makes sense.
   See `ObjectStorageSpi`

 * Java objects are used to define the schema. This will probably be relaxed
   in future.  See `StorageTypes`

 * All the objects are immutable.  You get the new values next time you read
   the object from the system.  Seems awkward, but it's designed to facilitate
   a remote editor (like a browser) sending a stream of key/field/value triples
   into the system.  It's NOT designed for rest-style POST-a-map.  See `WorkUnit`

 * The user defines interfaces only, and the system creates implementation classes
   dynamically.  See `Values`

 * There is some sort of weird bungled querying.  The API for this sucks but the
   implementation is pretty cool.  Basically it builds an index which is basically a
   `BitSet` and then ANDs/ORs those together to get the results.  Indexes keep 
   themselves up-to-date by listening to events. See `Query` and `Index`

 * A new API for querying is gestating.  See usages of the `@Finder` annotation.
   The cool thing here is that it translates the finder query into two parts.
   The first part goes to the aforementioned query subsystem.  (But not yet very
   smartly.)  The second translation is into an MVEL expression which does a
   secondary filter of the nodes.  This means your query & indexing can be 
   approximate or best-effort, as long as you error on the side of sending a
   little too much data.  So, you could switch from `BitSet` to `BloomFilter`.

## Neat code stuff

 * The parser underlying `@Finder` is a PEG parser generated by Parboiled.
   See `Grammar` for the definition and `FParser` for the use.

 * There's a `BloomFilter` implementation that works pretty good.  Key insight
   here is to use a super fast pseudo random number generator for the secondary
   bit positions.  It really works!

 * There's a series of classes implementing `Selection` which pick the top-N of
   a stream of values.  They don't require the full set of values in memory.  In 
   particular, QuickSelection is pretty fast, approaching `O(n)`.

 * There's cool class `IntPack` which implements (I think) a variant of P4Delta
   compression.  Anyway it can efficiently pack integers into a `byte[]` so you
   can keep, say, a sorted list of ids in memory in a very efficient way.

 * Last but not least, `Sequence` is how we keep things in a deterministic order
   across a set of systems in a basically-uncoordinated manner.  It does depend
   on then system clock though.  Damn your shitty clock, VMware!!

## About Log / Tuple / Stream vs. REST

I don't want to nag on REST at all, it's pretty awesome, but it has its optimum
uses cases and this has its use cases and can't we all just get along?

In particular, if you have a set of systems that have a memory representation
of something, and you want all of those systems to be in sync, a good pattern
to make that happen is for everybody to get a list of updates.  Maybe over a
queue.  This is natural with log-based systems.  

Of course, this isn't my idea: [LMAX Disruptor](http://martinfowler.com/articles/lmax.html)

Second, storage can be somewhat simplified.  Since the log is THE canonical 
version of events (ha ha, see what I did there?), and it's write-once, it's 
super easy to back up, move the log to a different system to replay under a 
debugger and to do a bunch of other cool things.  Consistency of the
system is based around that single `sync` on that file.  Filesystem are 
SUPER good at this.   Plus, free audit trail.

Of course, there's a name for this: [Event Sourcing](http://martinfowler.com/eaaDev/EventSourcing.html)

Third, I don't particularly care for UI which is based on the form-edit
post paradigm from 1970.  It's not like we have WYSE terminals any more, 
is it?  I prefer when the document is on screen and you just edit it.  You
can see this easily in flickr: just click a picture's title, it turns into
an edit box, and when you defocus, things are saved.  That's emitting an
id/field/value triple or I'm a monkey's uncle.





