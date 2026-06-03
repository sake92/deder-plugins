// Deder Dashboard — Cytoscape module dependency graph
(function() {
  function initGraph() {
    var cyEl = document.getElementById('cy');
    if (!cyEl || !window.__graphData) return;
    // prevent double init
    if (window.__cy) {
      try { window.__cy.destroy(); } catch(e) {}
    }

    var graphData = window.__graphData;
    if (!graphData.elements || graphData.elements.length === 0) return;

    var cy = cytoscape({
      container: cyEl,
      elements: graphData.elements,
      style: [
        {
          selector: 'node',
          style: {
            'background-color': 'data(color)',
            'label': 'data(label)',
            'font-size': '10px',
            'text-valign': 'center',
            'text-halign': 'center',
            'color': '#fff',
            'text-outline-width': 0,
            'width': 28,
            'height': 28,
            'border-width': 1,
            'border-color': '#fff'
          }
        },
        { selector: 'node[type="SCALA"]', style: { 'shape': 'ellipse' } },
        { selector: 'node[type="JAVA"]', style: { 'shape': 'ellipse' } },
        { selector: 'node[type="SCALA_TEST"]', style: { 'shape': 'round-rectangle' } },
        { selector: 'node[type="JAVA_TEST"]', style: { 'shape': 'round-rectangle' } },
        {
          selector: 'edge',
          style: {
            'width': 1,
            'line-color': '#aaa',
            'target-arrow-color': '#aaa',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier'
          }
        }
      ],
      layout: { name: 'cose-bilkent', animate: false, nodeRepulsion: 5000 }
    });

    window.__cy = cy;

    cy.on('tap', 'node', function(evt) {
      var node = evt.target;
      cy.elements().removeClass('highlighted');
      node.addClass('highlighted');
      node.neighborhood().addClass('highlighted');
    });

    cy.on('tap', function(evt) {
      if (evt.target === cy) {
        cy.elements().removeClass('highlighted');
      }
    });
  }

  document.addEventListener('alpine:init', initGraph);
  document.body.addEventListener('htmx:afterSettle', initGraph);
})();

// Called from Alpine.js @change on checkboxes
window.toggleGraphType = function(cy, type, show) {
  cy.nodes('[type="' + type + '"]').style('display', show ? 'element' : 'none');
};

// Called from Alpine.js @input on search field
window.graphSearch = function(cy, term) {
  cy.elements().removeClass('dimmed');
  if (!term || term.trim() === '') {
    cy.nodes().style('display', 'element');
    cy.fit();
    cy.center();
    return;
  }
  var lower = term.toLowerCase();
  cy.nodes().forEach(function(node) {
    if (node.data('id').toLowerCase().includes(lower)) {
      node.style('display', 'element');
      node.removeClass('dimmed');
    } else {
      node.style('display', 'none');
    }
  });
};

window.graphReset = function(cy) {
  cy.nodes().style('display', 'element');
  cy.fit();
  cy.center();
};
