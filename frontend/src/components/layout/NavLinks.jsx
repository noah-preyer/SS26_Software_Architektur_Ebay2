// navigationslinks der navbar, aktiver zustand kommt per prop vom server.

import { For } from "solid-js";

const LINKS = [
  { href: "/", label: "Startseite" },
  { href: "/my-listings", label: "Meine Anzeigen" },
  { href: "/my-orders", label: "Meine Käufe" },
];

export default function NavLinks(props) {
  const isActive = (href) =>
    href === "/" ? props.pathname === "/" : props.pathname.startsWith(href);

  if (props.mobile) {
    return (
      <div class="flex flex-col gap-1">
        <For each={LINKS}>
          {(link) => (
            <a
              href={link.href}
              class={`block px-3 py-2 rounded-md text-sm font-semibold transition-colors ${
                isActive(link.href)
                  ? "bg-[#E8400C] text-white"
                  : "text-white/70 hover:text-white hover:bg-white/10"
              }`}
              aria-current={isActive(link.href) ? "page" : undefined}
            >
              {link.label}
            </a>
          )}
        </For>
      </div>
    );
  }

  return (
    <div class="flex items-center gap-1">
      <For each={LINKS}>
        {(link) => (
          <a
            href={link.href}
            class={`px-4 py-2 rounded-md text-sm font-semibold transition-all duration-200 ${
              isActive(link.href)
                ? "bg-white text-[#111111]"
                : "text-white/70 hover:text-white hover:bg-white/10"
            }`}
            aria-current={isActive(link.href) ? "page" : undefined}
          >
            {link.label}
          </a>
        )}
      </For>
    </div>
  );
}
