# annoyED: Approximate nearest neighbors on your endless datastreams

AnnoyED is a streamified adoption of the Annoy algorithm [1, 2] by Erik Bernhardsson to retrieve approximate nearest neighbors. It continuously builds Annoy Indices on (possibly endless) data streams instead of on self-contained datasets only. The built indices can be queried during runtime (independent from the index building); it is so guaranteed that query results always contain the most current data. This project is implemented in Java, uses [Kafka Streams](https://kafka.apache.org/documentation/streams/), and was developed in the master’s seminar [“Machine Learning for Data Streams”](https://hpi.de/naumann/teaching/teaching/ws-1920/machine-learning-for-data-streams.html) at Hasso Plattner Institute in 2019/20. 

## Installation and Setup

Make sure to install the following:
- Java JDK (v8 or newer) ([↗ instructions](https://java.com/en/download/help/download_options.xml))
- Docker and Docker-Compose ([↗ instructions](https://docs.docker.com/compose/install/))
- Python3 and PIP3, optional for evaluation framework ([↗ instructions](https://www.python.org/downloads/))

Then:
1. Clone repository via `git clone git@github.com:jonashering/annoyED.git`.
2. Start the Docker daemon.
3. Run `docker-compose up`. This will start and setup Zookeeper and Kafka.
4. Execute `./gradlew run` to start the annoyED application.

## Usage

### REST Service

On startup, the application exposes three REST endpoints on `localhost:5000`. The relevant one, in the beginning, is `/params`. To configure the application you can make a POST request with the following body `<Number of Trees>;<Expected Dataset Size>;<Distance Metric ID>` onto this endpoint. The store allocates space for Expected Dataset Size datapoints; use 0 if this is unknown. 
We currently support 2 distance metrics: Euclidean (ID: 0) and Angular (ID: 1). 
An example body would be `10;0;0`. We would get 10 trees, no preallocated memory and the euclidean distance metric.

Be aware that every POST request resets the whole storage. This means that all data that was loaded into the store will be discarded.

The Kafka Streams application uses two topics: `source-topic` for input and `sink-topic` for output. We use the JSON format to write into and read from the topics. Messages to the `source-topic` should have the following format: 
```js
{
    'datapointID': 0,
    'vector':      [0.3,0.7,255,...],
    'persist':     True,
    'write':       True,
    'k':           10
 }

```

The `persist` and `write` flags are explained in the architecture section ([here](###Dataflow)). The application outputs JSON formatted strings to `sink-topic` with the following format:
```js
{
    list: [1,5,4,3]
 }
```

The contents of list are the `datapointID`s of the k nearest neighbors sorted by distance (the first one is the closest).

### Usage with ann-benchmarks

Erik Bernhardsson, the creator of Annoy, also developed a benchmarking framework called ann-benchmarks [3], which we use to evaluate our implementation. To run the benchmarks on AnnoyED, use our fork of ann-benchmarks found [↗ here](https://github.com/MariusDanner/ann-benchmarks) which includes a custom AnnoyED wrapper, i.e. datapoint producer, as well as the required configuration files. Then, follow the README there. In short this means:

```bash
git clone git@github.com:MariusDanner/ann-benchmarks.git  # clone repository
cd ann-benchmarks
pip install -r requirements.txt
python run.py  --local --dataset [dataset_id]# run the benchmark tool
python plot.py  # show and plot the results
```

## Why Approximate Nearest Neighbor Search for Data streams?

Finding similar records in datasets can facilitate solutions for many interesting use-cases. For example, recommendation engines for music services: We can recommend new interesting songs to listeners based on what other similar listeners previously listened to. Since both listeners are somewhat similar, it is not unlikely that they will perceive these recommendations as reasonably customized and meaningful.

K-NN formalizes searching similar points in a dataset as follows: Given a dataset of points in a vector space (e.g. a three-dimensional space), an arbitrary distance function (e.g. Manhattan distance), for each point _p_ in the dataset we want to retrieve the k points that are closest to _p_ based on the selected distance function, i.e. the k nearest neighbors [8]. Usually, these neighbors are then used for classification or regression tasks. 

A naive solution on a self-contained dataset is to compute distances between all pairs of points in the dataset and infer the k nearest neighbors from these results. This naive approach has quadratic time complexity wrt. the number of points in the dataset. While smart optimizations to this naive solution exist, the time complexity of k nearest neighbor search algorithms remains a bottleneck [7].

However for many use-cases, determining the exact k nearest neighbors is not a crucial requirement. For example when using k-NN for recommendation engines, it is usually reasonably good to compromise on quality, i.e. allow leaving out some true nearest neighbors, if it leads to a faster query performance. This is where approximate k-NN comes into place. Approximate nearest neighbor search is a relaxed definition of k-NN. Given a relative error of <img src="https://render.githubusercontent.com/render/math?math=\epsilon%20%3E%200"> a point _p_ is considered an approximate nearest neighbor of point _q_, if <img src="https://render.githubusercontent.com/render/math?math=dist(p,q)%20\leq%20(1%20%2B%20\epsilon)*dist(p^*,q)">, where _p*_  is a true nearest neighbor of _q_. Thus, _p_ is within relative error <img src="https://render.githubusercontent.com/render/math?math=\epsilon"> of the true nearest neighbor [7].

Approximate k-NN approaches were firstly developed for static datasets. When the underlying data changes, this requires however recalculating the entire model. This is costly and since models are then trained on snapshots of the dataset, queries might be answered based on deprecated data. To tackle this issue, we adopted the popular approximate k-NN approach Annoy [1, 2] and transformed it into a streamified approach.

## Related Work: Annoy, a solution for static datasets

Annoy [1, 2] uses a spatial indexing strategy with binary trees to store and retrieve points efficiently. Generally, the root node as well as every inner node in this tree represents a split, while every leaf node is a sink that holds a fixed number of points.
![Datenfluss](https://user-images.githubusercontent.com/15236859/76688617-c9e55480-662e-11ea-94bb-645508c90818.png)

**Building Annoy Index:** An index tree is built in a top-down procedure: Consider a set of points, i.e. your dataset, two points are randomly picked from this set. Between these two points, a bisecting hyperplane is constructed that is equidistant to both points. This split constitutes the root node in our index tree. It has a left and a right child node. Each point in the dataset is assigned to either one of the children nodes based on which side of the drawn hyperplane it is located.
This process of splitting the dataset into smaller and smaller chunks is recursively applied to every new child node until we have an index tree in which each leaf node holds at most a fixed number of points. The result of this process can be seen in the illustration above. In result, the constructed index trees with its split nodes and hyperplanes partition the space in which our points are located into small subspaces that are roughly equally populated.

**Querying Annoy Index:** Consider a query point for which you want to determine its k approximate nearest neighbors. We start with the root node of our previously built index tree and check on which side of its hyperplane our query point is located. Based on that result, we proceed to traverse the tree with either the left or right child node. We traverse the tree in this fashion until we reach a leaf node.
All points that this leaf node holds are considered nearest neighbor candidates. We proceed by calculating the distance to these points and the query point. The k points with the lowest distance are the resulting k nearest neighbors. 

**Using a forest instead of a single tree:** Our simplified explanation makes it easy to spot a major flaw in this concept: If we are unfortunate, splits could easily separate close points into different partitions, e.g. if they are very close to a hyperplane. For that reason, instead of constructing a single index tree, we construct a forest of n index trees that are all constructed randomly, i.e. each is different from one another. With every query, we search all index trees in this forest. By this, the problem of missing nearest neighbors due to unfortunate splits is reduced.

## Our approach: Streamifying Annoy

In the original implementation of Annoy, the built index is immutable. This results in it being impractical for an approach where the data originates from a continuous data stream rather than a static dataset. However, since the index trees constructed when building the index are simply binary trees, i.e. only leaves hold data and they do not need to be balanced, we can make them mutable and thus suitable for our approach with few modifications:

While the querying procedure remains unchanged, we add the following modification to constructing the index trees: Points can be added to an existing index tree at any time, the correct leaf node to hold the new point is found by traversing the index tree similarly to the querying process, i.e. until the corresponding subspace is found. Each leaf node has a fixed capacity. Until this capacity is reached, we can add new points to it. When it is reached, a new spit is introduced by inserting a new hyperplane in its space and partitioning the points into two leaves accordingly.

| **Step 1:** Traverse IndexTree to the corresponding leaf <br><br> ![](https://user-images.githubusercontent.com/15236859/76688619-cbaf1800-662e-11ea-86e4-f61908fec465.png) | **Step 2:** Insert new point at that leaf, split leaf if it reaches capacity (in this example: capacity=8) <br><br> ![](https://user-images.githubusercontent.com/15236859/76688621-d073cc00-662e-11ea-859d-442cd2fd3611.png) ![](https://user-images.githubusercontent.com/15236859/76688620-cfdb3580-662e-11ea-9adb-001a5a50374a.png) |
|---|---|

## Architecture and Components of annoyED

Apart from the Kafka-relevant components, we defined the following classes to store and retrieve approximate nearest neighbors:

<img src="https://user-images.githubusercontent.com/15236859/76688661-28123780-662f-11ea-9734-f74c14a2f81a.png" width=70%>

We implemented three data holding classes: `Datapoint`s have an _id_ which uniquely identifies them and is used as a key to store the `Datapoint` in a global lookup table. The _data_ vector holds the position of the datapoint in the vector space. The _persist_ flag tells the `NeighborProcessor` whether or not the datapoint should be written into the index. The _write_ flag tells the `NeighborProcessor` if the nearest neighbors should be retrieved and written to the `sink-topic`. The _k_ variable is only used if the _write_ flag is set and tells the `IndexStore` how many nearest neighbors should be retrieved.

### Dataflow

A `Datapoint` is written to the Kafka Topic `source-topic` through our `Producer` or an external application, i.e. the evaluation framework, to which the `NeighborProcessor` is subscribed. Depending on if the `Datapoint` should be written to the index and/or its nearest neighbors should be retrieved, it is proceeded as follows:

![Datenfluss](https://user-images.githubusercontent.com/15236859/76688615-c4880a00-662e-11ea-9072-93d9357c80f5.png)

**Adding a Datapoint to the Tree Index:** The `NeighborProcessor` calls the write method of the `IndexStore`. If the distance metric is angular, the vector of the `Datapoint` is converted to a unit vector as a preprocessing step. After this, the `Datapoint` gets inserted into a lookup table (HashMap) which maps the unique _datapointID_ to the `Datapoint`. The `Datapoint` is now being inserted into each one of the `IndexTree`s. If the `Datapoint` is the first point to be inserted into the tree, the parameter _ _k_, which determines the maximum capacity of a leaf, is set to the number of dimensions of the datapoint rounded up to the next hundred. Now, the correct `IndexNode` of each `IndexTree` is found by traversing the nodes of the tree starting by its root node and continuing with either its _leftChild_ or _rightChild_ based on which side of the `IndexSplit` the `Datapoint` is located. We stop when we find a leaf, i.e. a node without children.
The _datapointID_ is then added to the data list of the node. If the node data contains less than _ _k_ elements now, the insert operation is finished. Otherwise, a left and a right child node and an `IndexSplit` are created. First, two `Datapoint`s are selected to create the split randomly (or alternatively by an approximate 2-Means procedure). Then, a hyperplane (equidistant to both points) is constructed and persisted in the `IndexSplit`. After this, all `Datapoint`s of the node are moved either to the _leftChild_ or the _rightChild_ depending on which side of the hyperplane they are located.

**Querying the Nearest Neighbors of a Datapoint:** To query the k neighbors of the `Datapoint`, candidates from all trees are selected. First, a `HashMap<Integer, Double>` which maps the `datapointID` to the distance to the query point is created. It acts as a cache so that distances between the same pairs of `Datapoints` do not need to be calculated redundantly. In each `IndexTree`, the correct leaf node is determined in the procedure as on insert. Then, for each `Datapoint` in the leaf node, the distance to the query point is determined. This is done by looking up the _datapointID_ in the previously created cache or calculating the distance and caching it. Then, all `Datapoint`s of the leaf node are sorted by distance to the query point and the closest k `Datapoint`s are returned. In the last step, all returned `Datapoint`s (from all `IndexTrees`) are merged in a custom merge-sort-like procedure until the top k nearest neighbor `Datapoint`s have been determined. These are then returned and written to the `sink-topic` as `NeighborCandidates`.

## Experiments and their Results

The ann-benchmarks framework [3] provides access to multiple datasets and quality measures to evaluate approximate nearest neighbor approaches. We used it with a custom annoyED wrapper and measured _recall_ of the _k_ nearest neighbor result as <img src="https://render.githubusercontent.com/render/math?math=recall=\frac{|\{true%20NN\}%20\cap%20\{retrieved%20NN\}|}{k}"> (i.e. recall is equivalent to precision in this use case) as well as the number of processed queries per second as _QPS_ (this excludes model training) on these reference datasets:

| Dataset                      | Dimensions | Train size | Test size | Neighbors | Distance  |
| ---------------------------- | ---------: | ---------: | --------: | --------: | --------- |
| MNIST [4]                    |        784 |     60,000 |    10,000 |       100 | Euclidean |
| Fashion-MNIST [6]            |        784 |     60,000 |    10,000 |       100 | Euclidean |
| NYTimes (Bag of Words) [5]   |        256 |    290,000 |    10,000 |       100 | Angular   |

### Optimizations

In the process of developing annoyED, we started with a straightforward implementation of our approach and later implemented a number of small optimizations to increase both quality and throughput. We created benchmarks for these changes with the MNIST dataset [4], Euclidean distance and k = 10.

![](https://user-images.githubusercontent.com/23058484/76700077-ebd4ea80-66b3-11ea-9d2b-6ab76c7b5309.png)  ![](https://user-images.githubusercontent.com/23058484/76700076-eaa3bd80-66b3-11ea-864e-14e3a080ff33.png)

**Baseline:** Already with our baseline (_No Caching + Random_), where we select split candidates randomly and otherwise proceed as described in the previous section but leabing out the aforementioned query caching, we achieve a precision of up to 0.87 at 31 QPS with 5 index trees. As expected, with more trees our model can answer queries with higher recall, i.e. unfavorable partitions in one index tree are compensated by the other trees, but however at the cost of slower throughput due to the increased complexity.

**2-Means Split Candidate Selection**: Randomly selecting the points between which the splitting hyperplane is constructed in a split can lead to unbalanced splits, e.g. if a selected point is an outlier. This will then lead to an unbalanced and unnecessarily deep tree structure in our index. To approach this problem, we added a k-means approximation with two centroids to create an optimally splitting hyperplane.
With this optimization, we achieve a higher precision of up to 0.92 while at the same time maintaining the throughput of 31 QPS with 5 trees. We observe an increased QPS for less trees as well. Splitting the points in a more balanced way and thus creating more quality index trees is therefore a meaningful addition.

_However:_ When later running the experiments on the NYTimes dataset, we discovered that the 2-means algorithm might decrease the performance. Since the capacity of the index tree leaves depends on the dimensionality of points, low-dimensional datasets trigger splitting more often. The costly 2-means, as well as the large dataset size of NYTimes, makes the index building slower, we thus made it optional. 

**Caching intermediate distance results:** A quick recap of our implementation: When searching nearest neighbors, each index tree is traversed to the correct leaf node and from its set of points, the k closest points are forwarded as neighbor candidates where they are then merged to the true k results. Even though every index tree randomly partitions its points differently, it is likely that there is a common subset of neighbor candidates, i.e. the points closest, among all index trees. In our baseline implementation, this led to distances being calculated redundantly for every index tree. As an optimization, we cache the distances and so avoid duplicate computation. This does not lead to quality improvement, however, it improves the throughput to 40 QPS with 5 trees. In fact, we can now train more trees and while maintaining the same throughput we previously had with fewer trees, e.g. instead of 4 trees we now can have 5 at 40 _QPS_ increasing the recall by 0.04.

**Parallelization of Distance Calculation:** We also experimented with parallelizing the distance calculation when retrieving points from the index. Our implementation collected the set of neighbor candidates from all index trees and then distributed the distance calculation to all available cores using the Java interface `list.parallelStream().map(_ -> _)`. This did, unfortunately, decrease the speed. However, our testing setup is a machine with 4 (virtual) cores, so on a better-powered machine this might change.

### 10, 15, 25, 100 Nearest Neighbors

![](https://user-images.githubusercontent.com/23058484/76699565-e75a0300-66ae-11ea-8395-3a55405cf027.png) ![](https://user-images.githubusercontent.com/23058484/76699563-e1fcb880-66ae-11ea-9c2b-22872a67743c.png)

Additionally, we tested how well our implementation works with different settings of _k_. We observe, that the throughput remains relatively equal for different _k_. We expected this behavior, since sorting all leaf candidates is equally complex for every _k_, only the later merge step benefits from smaller _k_; however, its duration is insignificant. Also, that lower _k_ result in higher recall results is not surprising; due to the partitioning process very close points likely end up in the same partition, points further distant are more likely to be cut off to other partitions.

### MNIST, Fashion-MNIST and NYTimes Datasets

![](https://user-images.githubusercontent.com/23058484/76701723-eaabb980-66c3-11ea-904d-a9bd57a5c751.png)  ![](https://user-images.githubusercontent.com/23058484/76701725-eb445000-66c3-11ea-9d6c-2883a3953335.png)

With our implementation working reasonably well on MNIST with euclidean distance, we also tested it on the similar Fashion-MNIST dataset (euclidean distance) and on the NYTimes dataset (angular distance) with k = 10.  We generally observe that with a higher number of index trees, the _recall_ increases while the _QPS_ decreases.

The NYTimes dataset has 5 times the points as MNIST but only 256 dimensions. This results in a lower _\_k_ (than MNIST) and thus having less candidates per tree. This means that every tree holds a smaller fraction of available points (maximum 300/290000 = 0.001 (while for MNIST, each tree holds a maximum fraction of 0.013) so retrieving nearest neighbors is more difficult which is also confirmed by the recall results [3] of other algorithms.
Fashion-MNIST on the other hand performs very similar to MNIST. The reason for this is it being equal to MNIST in terms of dimensionality, size and the used distance measure. The insignificant differences are likely due to chance at runtime.

### AnnoyED vs. Annoy

<img src="https://user-images.githubusercontent.com/23058484/76700971-11b2bd00-66bd-11ea-9cc6-595afadfff93.png" width=70%>

Our project uses the core ideas from Annoy [1, 2], so we were interested in how well the different implementations compare on MNIST. Both implementations achieve similar high recall values; with 15 trees annoyED achieves a recall of 0.97, however at the price of few _QPS_ of 14.6. Annoy on the other hand achieves an recall of 0.99 with 83.3 _QPS_. Another example: to get a recall of 0.79 annoyED has 30.2 _QPS_ while Annoy achieves 3940 _QPS_.

To consider is the following: Annoy is a highly optimized library written in C++, it uses AVX [9] where possible to accelerate for-loops using SIMD operations. Also, Annoy does not allow modifying built indices which allows further optimizations. Plus, Annoy is directly accessed through a C++/Python interface while annoyED uses Kafka topics which leads to networking overhead. 

## Discussion and Future Work

We implemented an approximate nearest neighbor search prototype for data streams with Kafka Streams. Our solution is based on the Annoy algorithm [1, 2] and adopts its key ideas. As opposed to Annoy, annoyED does not implement separate model training and inference phases but its indices can be extended and queried continuously. Therefore, it is especially suitable when nearest neighbor search is realized on volatile data, e.g. data streams. AnnoyED works reasonably well on a number of different datasets and supports multiple distance measures. However, its performance is not on par with the highly optimized Annoy library.

Implementing annoyED, Kafka Streams turned out to be a suitable framework. While we needed to develop a custom state stores and processor, the Kafka Streams API allowed a flexible architecture according to our requirements. However, we expect adding a reasonable windowing strategy, which is not yet included, to be an elaborate effort due to its complexity.

Current limitations to consider are the following: We only ran and evaluated annoyED in a non-distributed setting, thus adding a layer of parallelization by e.g. multiplying the number of index stores and running them on separate instances is a task for future work. Also, a custom windowing strategy to allow for old data to be forgotten and thus account for e.g. concept drifts is an important step before running annoyED in a production setting.

## References and Further Reading

[1]: Bernhardsson, E. (2015). “Nearest Neighbor Methods And Vector Models: Part 1&2” (Blogpost). https://erikbern.com/2015/09/24/nearest-neighbor-methods-vector-models-part-1, https://erikbern.com/2015/10/01/nearest-neighbors-and-vector-models-part-2-how-to-search-in-high-dimensional-spaces.html.

[2]: Spotify Inc. (since 2013). “Annoy: Approximate Nearest Neighbors Oh Yeah” (Repository). https://github.com/spotify/annoy.

[3]: Aumüller, M.; Bernhardsson, E.; Faithfull, A.J. (2018). “ANN-Benchmarks: A Benchmarking Tool for Approximate Nearest Neighbor Algorithms” (PDF). CoRR, abs/1807.05614. Also: http://ann-benchmarks.com/, https://github.com/erikbern/ann-benchmarks/.

[4]: LeCun, Y.; Cortes, C; Burges, CJ (2010). “MNIST handwritten digit database” (Online). ATT Labs. http://yann.lecun.com/exdb/mnist/.

[5]: Newman, D. (2008). “Bag of Words Data Set: NYTimes” (Online). University of California, Irvine. https://archive.ics.uci.edu/ml/datasets/bag+of+words. 

[6]: Xiao, H.; Rasul, K; Vollgraf, R. (2017). “Fashion-MNIST: a Novel Image Dataset for Benchmarking Machine Learning Algorithms” (PDF). CoRR, abs/1708.07747. Also: https://github.com/zalandoresearch/fashion-mnist.

[7]: Arya, S.; Mount, D. M.; Netanyahu, N. S.; Silverman, R.; Wu, A. (1998). "An optimal algorithm for approximate nearest neighbor searching" (PDF). Journal of the ACM. 45 (6): 891–923.

[8]: Dasarathy, Belur V., ed. (1991). Nearest Neighbor (NN) Norms: NN Pattern Classification Techniques. ISBN 978-0-8186-8930-7.

[9]: Intel Corp. (2020). "Intel® Advanced Vector Extensions 512" (Online). https://www.intel.com/content/www/us/en/architecture-and-technology/avx-512-overview.html.
