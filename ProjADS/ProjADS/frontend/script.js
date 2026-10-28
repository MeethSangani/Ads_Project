// Simple vanilla JavaScript for BTech ADS mini-project

document.addEventListener("DOMContentLoaded", function () {
    loadStats();
    executeSearch();

    document.getElementById("search-form").addEventListener("submit", function (e) {
        e.preventDefault();
        executeSearch();
    });

    document.getElementById("btn-reset").addEventListener("click", function () {
        document.getElementById("min-price").value = "5000000";
        document.getElementById("max-price").value = "15000000";
        document.getElementById("min-area").value = "";
        document.getElementById("max-area").value = "";
        document.getElementById("bedrooms").value = "0";
        document.getElementById("location").value = "All";
        document.getElementById("property-type").value = "All";
        document.getElementById("algorithm-select").value = "bst";
        executeSearch();
    });

    document.getElementById("insert-form").addEventListener("submit", handleInsert);
    document.getElementById("delete-form").addEventListener("submit", handleDelete);
});

// Load Tree Stats
function loadStats() {
    fetch("/api/stats")
        .then(res => res.json())
        .then(data => {
            document.getElementById("stat-count").innerText = data.totalProperties;
            document.getElementById("stat-bst-height").innerText = data.bstHeight;
            document.getElementById("stat-avl-height").innerText = data.avlHeight;
        })
        .catch(err => console.error("Could not fetch tree stats:", err));
}

// Search Properties
function executeSearch() {
    const minPrice = document.getElementById("min-price").value || "0";
    const maxPrice = document.getElementById("max-price").value || "500000000";
    const minArea = document.getElementById("min-area").value || "0";
    const maxArea = document.getElementById("max-area").value || "0";
    const bedrooms = document.getElementById("bedrooms").value || "0";
    const location = document.getElementById("location").value;
    const propertyType = document.getElementById("property-type").value;
    const algo = document.getElementById("algorithm-select").value;

    const query = new URLSearchParams({
        minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType, algorithm: algo
    });

    const tbody = document.getElementById("results-body");
    const info = document.getElementById("search-time-info");
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">Searching...</td></tr>`;

    fetch("/api/search?" + query.toString())
        .then(res => res.json())
        .then(data => {
            info.innerText = `Algorithm: ${data.algorithm} | Matches: ${data.count} | Time: ${data.timeNs} ns (${data.timeMs.toFixed(3)} ms)`;

            tbody.innerHTML = "";
            if (!data.properties || data.properties.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No properties found.</td></tr>`;
                return;
            }

            data.properties.forEach(p => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${p.id}</td>
                    <td>${p.location}</td>
                    <td>${p.formattedPrice}</td>
                    <td>${p.area}</td>
                    <td>${p.bedrooms} BHK</td>
                    <td>${p.propertyType}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:red;">Server error. Ensure Java application is running.</td></tr>`;
        });
}

// Insert Property
function handleInsert(e) {
    e.preventDefault();
    const id = document.getElementById("ins-id").value.trim();
    const location = document.getElementById("ins-location").value;
    const price = document.getElementById("ins-price").value;
    const area = document.getElementById("ins-area").value;
    const bedrooms = document.getElementById("ins-bedrooms").value;
    const propertyType = document.getElementById("ins-type").value;

    const msg = document.getElementById("ins-msg");

    fetch("/api/insert", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id, location, price, area, bedrooms, propertyType })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            msg.className = "msg success";
            msg.innerText = `Property ${id} inserted successfully! (BST Height: ${data.bstHeight}, AVL Height: ${data.avlHeight})`;
            document.getElementById("insert-form").reset();
            loadStats();
        } else {
            msg.className = "msg error";
            msg.innerText = "Error inserting property.";
        }
    })
    .catch(err => {
        msg.className = "msg error";
        msg.innerText = "Network error.";
    });
}

// Delete Property
function handleDelete(e) {
    e.preventDefault();
    const id = document.getElementById("del-id").value.trim();
    const msg = document.getElementById("del-msg");

    fetch("/api/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            msg.className = "msg success";
            msg.innerText = `Property ${data.id} deleted successfully! (BST Height: ${data.bstHeight}, AVL Height: ${data.avlHeight})`;
            document.getElementById("del-id").value = "";
            loadStats();
        } else {
            msg.className = "msg error";
            msg.innerText = `Property '${id}' not found.`;
        }
    })
    .catch(err => {
        msg.className = "msg error";
        msg.innerText = "Network error.";
    });
}

// Run Benchmark
function runBenchmark(count) {
    const status = document.getElementById("benchmark-status");
    const tbody = document.getElementById("benchmark-body");

    status.innerText = `Benchmarking ${count} properties with System.nanoTime()...`;
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">Running...</td></tr>`;

    fetch(`/api/benchmark?count=${count}`)
        .then(res => res.json())
        .then(data => {
            status.innerText = `Benchmark done for ${count} records.`;
            tbody.innerHTML = "";

            data.results.forEach(r => {
                const tr = document.createElement("tr");
                const h = (r.treeHeight === 0) ? "N/A" : r.treeHeight;
                tr.innerHTML = `
                    <td>${r.algorithm}</td>
                    <td>${data.count}</td>
                    <td>${r.buildTimeMs.toFixed(3)}</td>
                    <td>${r.singleSearchTimeUs.toFixed(3)}</td>
                    <td>${r.rangeSearchTimeUs.toFixed(3)}</td>
                    <td>${h}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            status.innerText = "Error running benchmark.";
        });
}

// Run Sorted Insertion Experiment
function runSortedExperiment() {
    const status = document.getElementById("benchmark-status");
    status.innerText = "Running sorted insertion experiment (1,000 items)...";

    fetch("/api/skewed-experiment?count=1000")
        .then(res => res.json())
        .then(data => {
            status.innerText = "Experiment completed.";
            document.getElementById("sorted-box").style.display = "block";
            document.getElementById("sort-bst-h").innerText = data.bstHeight;
            document.getElementById("sort-bst-t").innerText = data.bstSearchTimeNs + " ns";
            document.getElementById("sort-avl-h").innerText = data.avlHeight;
            document.getElementById("sort-avl-t").innerText = data.avlSearchTimeNs + " ns";
        })
        .catch(err => {
            status.innerText = "Error running sorted experiment.";
        });
}
