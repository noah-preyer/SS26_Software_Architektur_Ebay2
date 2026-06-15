// bilder zuschneiden und komprimieren, werden als data-url in imageUrls[] gespeichert.
// das backend braucht keinen upload-endpunkt solange die payload-größe passt (max ~0.75mb pro inserat).

import imageCompression from "browser-image-compression";

export const MAX_IMAGES = 3;
export const IMAGE_ASPECT_RATIO = 4 / 3;

const COMPRESSION_OPTIONS = {
  maxWidthOrHeight: 800,
  maxSizeMB: 0.25,
  useWebWorker: true,
};

// komprimierung nicht überspringen, unkomprimierte bilder sind zu groß für den request.
export async function compressToDataUrl(blob) {
  const compressed = await imageCompression(blob, COMPRESSION_OPTIONS);
  return blobToDataUrl(compressed);
}

function blobToDataUrl(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(blob);
  });
}
