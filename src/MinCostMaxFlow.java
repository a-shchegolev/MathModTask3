import java.util.*;

public class MinCostMaxFlow
{
    /// Вспомогательные классы и методы
    static class Edge
    {
        public int to;
        public int rev;

        public int cap;
        public int cost;
        public int flow;

        public Edge(int to, int rev, int cap, int cost)
        {
            this.to = to;
            this.rev = rev;
            this.cap = cap;
            this.cost = cost;
            this.flow = 0;
        }
    }

    static class Graph
    {
        public final List<List<Edge>> adj;

        public Graph(int size)
        {
            adj = new ArrayList<>(size);
            for (int i = 0; i < size; i++)
            {
                adj.add(new ArrayList<>());
            }
        }

        public void addEdge(int from, int to, int cap, int cost)
        {
            Edge forward = new Edge(to, adj.get(to).size(), cap, cost);
            Edge backward = new Edge(from, adj.get(from).size(), 0, -cost);
            adj.get(from).add(forward);
            adj.get(to).add(backward);
        }
    }

    public record ResultIntPair(int totalFlow, int totalCost) { }

    private static final int INF = Integer.MAX_VALUE;
    private static final Map<String, Integer> nodeMap = new HashMap<>();

    private static int findEdge(String name)
    {
        if (!nodeMap.containsKey(name))
        {
            nodeMap.put(name, nodeMap.size());
        }
        return nodeMap.get(name);
    }

    private static String getNameById(int id)
    {
        for (var entry : nodeMap.entrySet())
        {
            if (entry.getValue() == id)
            {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void main(String[] args)
    {
        Integer[][] edgeCaps = {
                {null, 30, 45, 25, 30, 20, 40},
                {30, null, 55, 25, 35, 40, 25},
                {25, 30, null, 45, 75, 30, 40},
                {15, 10, 25, null, 40, 30, 80},
                {10, 45, 15, 60, null, 60, 75},
                {10, 30, 45, 30, 55, null, 40},
                {15, 25, 45, 30, 40, 50, null}
        };

        Integer[][] edgeCosts = {
                {null, 5, 10, 4, 5, 6, 10},
                {1, null, 7, 10, 15, 5, 5},
                {1, 10, null, 5, 4, 7, 12},
                {2, 6, 4, null, 5, 10, 8},
                {1, 7, 4, 4, null, 9, 2},
                {1, 4, 2, 3, 8, null, 12},
                {1, 10, 5, 6, 8, 16, null}
        };

        var nodeCaps = new int[] {1000, 55, 35, 40, 50, 45, 1000};
        int n = 7;

        final var g = new Graph(n * 2);

        for (int i = 0; i < n; i++)
        {
            int inNode = findEdge("n" + (i + 1) + "-in");
            int outNode = findEdge("n" + (i + 1) + "-out");
            g.addEdge(inNode, outNode, nodeCaps[i], 0);
        }

        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                if (i != j && edgeCaps[i][j] != null && edgeCaps[i][j] > 0)
                {
                    int from = findEdge("n" + (i + 1) + "-out");
                    int to = findEdge("n" + (j + 1) + "-in");
                    g.addEdge(from, to, edgeCaps[i][j], edgeCosts[i][j]);
                }
            }
        }

        int source = findEdge("n1-in");
        int sink = findEdge("n7-out");

        ResultIntPair result = minCostMaxFlow(g, source, sink);

        System.out.println("Максимальное кол-во поездов: " + result.totalFlow);
        System.out.println("Минимальная стоимость проезда c максимальным кол-вом поездов: " + result.totalCost);

        for (var u = 0; u < g.adj.size(); u++)
        {
            for (var e : g.adj.get(u))
            {
                if (e.flow > 0 && e.cost >= 0)
                {
                    String fromName = getNameById(u);
                    String toName = getNameById(e.to);
                    System.out.println(fromName + " => " + toName + ": " + e.flow);
                }
            }
        }
    }

    public static ResultIntPair minCostMaxFlow(final Graph graph, int s, int t)
    {
        int N = graph.adj.size();
        int totalFlow = 0;
        int totalCost = 0;
        int iteration = 0;

        int autCosts = 0;
        StringBuilder resultBuilder = new StringBuilder();
        while (true)
        {
            var dist = new int[N];
            var prevNode = new int[N];
            var prevEdge = new int[N];
            Arrays.fill(dist, INF);
            Arrays.fill(prevNode, -1);
            Arrays.fill(prevEdge, -1);
            dist[s] = 0;

            var updated = true;
            for (var k = 0; k < N; k++)
            {
                if (!updated)
                {
                    break;
                }

                updated = false;
                for (var u = 0; u < N; u++)
                {
                    if (dist[u] == INF)
                    {
                        continue;
                    }
                    for (var i = 0; i < graph.adj.get(u).size(); i++)
                    {
                        var edge = graph.adj.get(u).get(i);
                        if (edge.cap - edge.flow > 0 && dist[edge.to] > dist[u] + edge.cost)
                        {
                            dist[edge.to] = dist[u] + edge.cost;
                            prevNode[edge.to] = u;
                            prevEdge[edge.to] = i;
                            updated = true;
                        }
                    }
                }
            }

            if (dist[t] == INF)
                break;

            var augFlow = INF;
            for (var v = t; v != s; v = prevNode[v])
            {
                var u = prevNode[v];
                var edge = graph.adj.get(u).get(prevEdge[v]);
                augFlow = Math.min(augFlow, edge.cap - edge.flow);
            }

            var path = new ArrayList<int[]>();
            for (var v = t; v != s; v = prevNode[v])
            {
                var u = prevNode[v];
                path.add(new int[]{u, v});
            }

            Collections.reverse(path);
            if (iteration < 5)
            {

                resultBuilder.append("\nИтерация: ").append(iteration + 1).append("\nПуть: ");

                var isFirstIter = true;
                for (var edge : path) {
                    var u = edge[0];
                    var v = edge[1];
                    String fromName = getNameById(u);
                    String toName = getNameById(v);
                    Edge e = null;
                    for (var edgeItem : graph.adj.get(u)) {
                        if (edgeItem.to == v) {
                            e = edgeItem;
                            break;
                        }
                    }
                    assert e != null;
                    if (isFirstIter)
                    {
                        resultBuilder.append(fromName).append(" → ").append(toName).append(" →");
                        isFirstIter = false;
                    }
                    else if (Objects.equals(toName, "n7-out"))
                    {
                        resultBuilder.append(" ").append(toName);
                    }
                    else
                    {
                        resultBuilder.append(" ").append(toName).append(" →");
                    }
                }
                resultBuilder.append("\nНайден поток: ").append(augFlow).append("\n");
                autCosts += augFlow;

                for (int[] edge : path)
                {
                    var u = edge[0];
                    var v = edge[1];
                    for (var edgeTo : graph.adj.get(u))
                    {
                        if (edgeTo.to == v)
                        {
                            break;
                        }
                    }
                }
                resultBuilder.append("Суммарный поток на этой итерации: ").append(autCosts).append("\n");
            }

            for (var v = t; v != s; v = prevNode[v])
            {
                var u = prevNode[v];
                var edge = graph.adj.get(u).get(prevEdge[v]);
                edge.flow += augFlow;
                graph.adj.get(edge.to).get(edge.rev).flow -= augFlow;
                totalCost += augFlow * edge.cost;
            }

            totalFlow += augFlow;
            iteration++;
        }
        System.out.println(resultBuilder);
        return new ResultIntPair(totalFlow, totalCost);
    }
}