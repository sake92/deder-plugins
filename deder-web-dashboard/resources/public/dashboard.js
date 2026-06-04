// Deder Dashboard — Cytoscape graph helpers

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
