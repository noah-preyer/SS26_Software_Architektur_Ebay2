// formular zum anlegen und bearbeiten von anzeigen, inkl. bild-zuschneiden und komprimierung.
// Ohne Bild wird automatisch ein Picsum-Platzhalter gesetzt.

import { createSignal, Show, For } from "solid-js";
import { KATEGORIEN } from "../../lib/mockData.js";
import { createProduct, updateProduct, errorMessage } from "../../lib/api.js";
import { getUserInfo } from "../../lib/auth.js";
import { compressToDataUrl, MAX_IMAGES, IMAGE_ASPECT_RATIO } from "../../lib/images.js";
import ImageCropper from "../media/ImageCropper.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";

const MAX_TITLE_LENGTH = 80;
const MAX_DESCRIPTION_LENGTH = 1000;

export default function ProductForm(props) {
  const editing = () => !!props.product;

  const [title, setTitle] = createSignal(props.product?.title ?? "");
  const [description, setDescription] = createSignal(props.product?.description ?? "");
  const [price, setPrice] = createSignal(props.product?.price ?? "");
  const [category, setCategory] = createSignal(props.product?.category ?? "");
  const [images, setImages] = createSignal(props.product?.imageUrls ?? []);

  const [cropSrc, setCropSrc] = createSignal(null);
  const [compressing, setCompressing] = createSignal(false);

  const [titleError, setTitleError] = createSignal("");
  const [priceError, setPriceError] = createSignal("");
  const [error, setError] = createSignal("");
  const [loading, setLoading] = createSignal(false);

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    e.target.value = ""; // erlaubt erneutes Auswählen derselben Datei
    if (!file) return;

    if (images().length >= MAX_IMAGES) {
      setError(`Maximal ${MAX_IMAGES} Bilder pro Anzeige.`);
      return;
    }

    setError("");
    setCropSrc(URL.createObjectURL(file));
  };

  const handleCrop = async (blob) => {
    setCropSrc(null);
    setCompressing(true);
    try {
      const dataUrl = await compressToDataUrl(blob);
      setImages((prev) => [...prev, dataUrl]);
    } catch {
      setError("Bild konnte nicht verarbeitet werden.");
    } finally {
      setCompressing(false);
    }
  };

  const handleCancelCrop = () => {
    setCropSrc(null);
  };

  const removeImage = (index) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setTitleError("");
    setPriceError("");

    const trimmedTitle = title().trim();
    const priceNum = Number(price());

    let hasError = false;
    if (!trimmedTitle) {
      setTitleError("Bitte einen Titel angeben.");
      hasError = true;
    } else if (trimmedTitle.length > MAX_TITLE_LENGTH) {
      setTitleError(`Titel darf maximal ${MAX_TITLE_LENGTH} Zeichen lang sein.`);
      hasError = true;
    }
    if (!price() || priceNum <= 0) {
      setPriceError("Bitte einen Preis größer als 0 angeben.");
      hasError = true;
    }
    if (hasError) return;

    setLoading(true);
    try {
      const finalImages =
        images().length > 0 ? images() : [`https://picsum.photos/seed/product-${Date.now()}/400/300`];

      const payload = {
        title: trimmedTitle,
        description: description().trim() || null,
        price: priceNum,
        category: category() || null,
        imageUrls: finalImages,
      };

      if (editing()) {
        await updateProduct(props.product.id, payload);
      } else {
        // das echte backend sollte die sellerid aus dem jwt lesen, nicht vom client annehmen.
        payload.sellerId = Number(getUserInfo()?.id);
        await createProduct(payload);
      }

      props.onSuccess?.();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div class="card p-6">
      <h2 class="text-2xl font-bold mb-4">{editing() ? "Anzeige bearbeiten" : "Neue Anzeige"}</h2>

      <form class="space-y-5" onSubmit={handleSubmit}>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="title">
            Titel
          </label>
          <input
            id="title"
            type="text"
            required
            maxLength={MAX_TITLE_LENGTH}
            class="input-field"
            value={title()}
            onInput={(e) => setTitle(e.target.value)}
          />
          <div class="flex justify-between mt-1">
            <Show when={titleError()}>
              <p class="text-xs text-red-600">{titleError()}</p>
            </Show>
            <p class="text-xs text-[#999] ml-auto">
              {title().length}/{MAX_TITLE_LENGTH}
            </p>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="description">
            Beschreibung
          </label>
          <textarea
            id="description"
            rows="5"
            maxLength={MAX_DESCRIPTION_LENGTH}
            class="input-field resize-y"
            placeholder="Zustand, Alter, Abholung/Versand …"
            value={description()}
            onInput={(e) => setDescription(e.target.value)}
          />
          <p class="text-xs text-[#999] text-right mt-1">
            {description().length}/{MAX_DESCRIPTION_LENGTH}
          </p>
        </div>

        <div class="grid sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-bold text-[#111111] mb-1.5" for="price">
              Preis (€)
            </label>
            <input
              id="price"
              type="number"
              min="0"
              step="0.01"
              required
              class="input-field"
              value={price()}
              onInput={(e) => setPrice(e.target.value)}
            />
            <Show when={priceError()}>
              <p class="text-xs text-red-600 mt-1">{priceError()}</p>
            </Show>
          </div>

          <div>
            <label class="block text-sm font-bold text-[#111111] mb-1.5" for="category">
              Kategorie
            </label>
            <select
              id="category"
              class="input-field"
              value={category()}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="">-- Bitte wählen --</option>
              <For each={KATEGORIEN}>{(kat) => <option value={kat}>{kat}</option>}</For>
            </select>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5">
            Bilder ({images().length}/{MAX_IMAGES})
          </label>
          <div class="flex flex-wrap gap-3 mb-2">
            <For each={images()}>
              {(img, index) => (
                <div class="relative">
                  <img src={img} alt="" class="w-24 h-24 object-cover rounded-xl" />
                  <Show when={index() === 0}>
                    <span class="absolute top-1 left-1 bg-[#E8400C] text-white text-[10px] font-black px-1.5 py-0.5 rounded-sm border border-[#111111]">
                      Hauptbild
                    </span>
                  </Show>
                  <button
                    type="button"
                    onClick={() => removeImage(index())}
                    class="absolute -top-2 -right-2 bg-[#111111] text-white rounded-full w-6 h-6 flex items-center justify-center text-sm font-bold hover:bg-[#E8400C] transition-colors"
                    aria-label="Bild entfernen"
                  >
                    ×
                  </button>
                </div>
              )}
            </For>
          </div>

          <Show when={images().length < MAX_IMAGES}>
            <label class="btn-secondary inline-block cursor-pointer">
              {compressing() ? "Wird verarbeitet..." : "Bild hinzufügen"}
              <input type="file" accept="image/*" class="hidden" onChange={handleFileChange} disabled={compressing()} />
            </label>
          </Show>
          <p class="text-xs text-[#999] mt-1">Ohne Bild wird automatisch ein Platzhalter verwendet.</p>
        </div>

        <Show when={error()}>
          <ErrorBanner type="error" message={error()} />
        </Show>

        <div class="flex gap-3">
          <button type="submit" disabled={loading()} class="btn-primary">
            {loading() ? "Speichern..." : editing() ? "Speichern" : "Anzeige erstellen"}
          </button>
          <button type="button" onClick={() => props.onCancel?.()} class="btn-secondary">
            Abbrechen
          </button>
        </div>
      </form>

      <Show when={cropSrc()}>
        <ImageCropper imageSrc={cropSrc()} aspectRatio={IMAGE_ASPECT_RATIO} onCrop={handleCrop} onCancel={handleCancelCrop} />
      </Show>
    </div>
  );
}
