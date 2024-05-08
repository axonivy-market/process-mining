function removeExecutedClass() {
  getProcessDiagramIframe().find(".executed").removeClass("executed");
}

function removeDefaultFrequency() {
  getProcessDiagramIframe()
    .find(".execution-badge")
    .each(function () {
      $(this).parent().remove();
    });
}

function santizeDiagram() {
  removeDefaultFrequency();
  removeExecutedClass();
  addElementFrequency("18F4D3E3B39C44F1-f3", "2");
  addElementFrequency("18F4D3E3B39C44F1-f0", "2");
  addElementFrequency("18F4D3E3B39C44F1-f1", "2");
}

function getProcessDiagramIframe() {
  return $("#process-diagram-iframe").contents();
}

function addElementFrequency(elementId, frequencyRatio) {
  getProcessDiagramIframe()
    .find(`#sprotty_${elementId}`)
    .append(
      `<svg>
        <g>
          <rect rx="7" ry="7" x="19" y="20" width="30" height="14" class="execution-badge">
          </rect><text x="34" y="26" dy=".4em" class="execution-text">${frequencyRatio}</text>
        </g>
      </svg>`
    );
}

function addElementsFrequency() {}
