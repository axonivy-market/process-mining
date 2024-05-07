function removeExecutedClass() {
  $("#process-diagram-iframe")
    .contents()
    .find(".executed")
    .removeClass("executed");
}

function removeDefaultFrequency() {
  $("#process-diagram-iframe")
    .contents()
    .find(".execution-badge")
    .each(function () {
      $(this).parent().remove();
    });
}

function santizeDiagram() {
    removeDefaultFrequency();
    removeExecutedClass();
}

function addElementFrequency() {}

function addElementsFrequency() {}
