// Deder Dashboard — Cytoscape graph helpers

// Called from Alpine.js @change on checkboxes — reads all 5 filter states from Alpine scope
window.applyFilters = function(cy) {
  if (!cy) return;
  var rootEl = document.querySelector('[x-data]');
  if (!rootEl) return;
  var state = Alpine.$data(rootEl);

  cy.nodes().forEach(function(node) {
    var type = node.data('type');
    var show = false;

    // Platform check: which toggle controls this node type?
    if (type === 'SCALA' || type === 'SCALA_TEST') {
      show = state.showScala;
    } else if (type === 'JAVA' || type === 'JAVA_TEST') {
      show = state.showJava;
    } else if (type === 'SCALA_JS' || type === 'SCALA_JS_TEST') {
      show = state.showScalaJs;
    } else if (type === 'SCALA_NATIVE' || type === 'SCALA_NATIVE_TEST') {
      show = state.showScalaNative;
    } else {
      show = true; // UNKNOWN type always visible
    }

    // Test filter: if it's a test module and Test toggle is OFF, hide it
    if (show && !state.showTest && type.indexOf('_TEST') !== -1) {
      show = false;
    }

    node.style('display', show ? 'element' : 'none');
  });
};

// Called from Alpine.js @input on search field
window.graphSearch = function(cy, term) {
  cy.elements().removeClass('dimmed');
  if (!term || term.trim() === '') {
    cy.nodes().style('display', 'element');
    window.applyFilters(cy); // re-apply filters after showing all
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
  window.applyFilters(cy);
  cy.fit();
  cy.center();
};
