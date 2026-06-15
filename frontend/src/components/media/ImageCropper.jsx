// modal zum zuschneiden eines bildes auf das gewünschte seitenverhältnis.

import { createSignal, onMount, onCleanup, Show } from "solid-js";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";

const ImageCropper = (props) => {
  const [showOverlay, setShowOverlay] = createSignal(true);
  const [errorMessage, setErrorMessage] = createSignal("");
  let imageRef;
  let cropperInstance;

  onMount(() => {
    if (imageRef) {
      cropperInstance = new Cropper(imageRef, {
        aspectRatio: props.aspectRatio || 1,
        viewMode: 1,
        dragMode: "move",
        autoCropArea: 1,
        restore: false,
        modal: false,
        guides: true,
        highlight: false,
        cropBoxMovable: true,
        cropBoxResizable: true,
        toggleDragModeOnDblclick: false,
      });
    }
  });

  onCleanup(() => {
    if (cropperInstance) {
      cropperInstance.destroy();
    }
  });

  const cropImage = () => {
    if (!cropperInstance) return;

    const croppedCanvas = cropperInstance.getCroppedCanvas();
    croppedCanvas.toBlob((blob) => {
      if (blob.size > 10 * 1024 * 1024) {
        setErrorMessage("Das zugeschnittene Bild ist größer als 10MB. Bitte ein kleineres Bild wählen.");
        return;
      }

      const url = URL.createObjectURL(blob);
      props.onCrop?.(blob, url);
      setShowOverlay(false);
    }, "image/jpeg", 0.9);
  };

  const cancelCrop = () => {
    setShowOverlay(false);
    props.onCancel?.();
  };

  return (
    <Show when={showOverlay()}>
      <div class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="card overflow-hidden w-full max-w-md">
          <h2 class="text-xl font-bold p-4 bg-[#111111] text-white">
            Bild zuschneiden
          </h2>
          <div class="p-4">
            <div class="relative w-full" style={{ height: "240px" }}>
              <img
                ref={imageRef}
                src={props.imageSrc}
                alt="Zuzuschneidendes Bild"
                class="max-w-full h-full object-contain"
              />
            </div>
            <Show when={errorMessage()}>
              <p class="text-red-500 mt-2">{errorMessage()}</p>
            </Show>
            <div class="flex justify-end space-x-4 mt-4">
              <button type="button" onClick={cancelCrop} class="btn-secondary">
                Abbrechen
              </button>
              <button type="button" onClick={cropImage} class="btn-primary">
                Zuschneiden
              </button>
            </div>
          </div>
        </div>
      </div>
    </Show>
  );
};

export default ImageCropper;
