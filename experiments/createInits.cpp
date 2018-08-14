#include "bits/stdc++.h"
using namespace std;

vector<vector<int>> g;
vector<int> visited;

int main(int argv, char** argc) {
    int n = 0;
    int m = 0;
    int a, b;
    int start;
    while ((cin >> a) and (cin >> b)) {
        n = max(n, a + 1); n = max(n, b + 1);
        if (m == 0) {
            start = a;
        }
        m += 1;
        g.resize(n);
        g[a].push_back(b);
    }
    visited.assign(n, 0);

    stack<int> recursion;

    for (int i = 0; i < n; i++) {
        int u = (i + start) % n;
        if (visited[u]) {
            continue;
        }
        cout << u << endl;
        recursion.push(u);
START:
        if (recursion.empty()) {
            goto END;
        }
        u = recursion.top();
        visited[u] = 1;
        recursion.pop();
        for (int v: g[u]) {
            if (visited[v]) {
                continue;
            }
            recursion.push(v);
        }
        goto START;
END:
        ;
    }
    return 0;
}

