import { KB_VERSION } from "@/lib/siginex/modulos-sgi";

/**
 * Topbar de la home `/siginex`, clon visual del Topbar del repo de referencia
 * (Siginex). Usa colores HEX literales para preservar la paleta navy + dorado
 * (los tokens de concientic resuelven a otros colores).
 */
export function HomeTopbar({ titulo }: { titulo: string }) {
  return (
    <header
      className="sticky top-0 z-20 flex items-center justify-between gap-4 border-b border-[#E1E7F0] px-6 py-3.5 backdrop-blur"
      style={{ background: "rgba(237,241,247,0.9)" }}
    >
      <div>
        <p className="font-mono text-[10px] uppercase tracking-[1px] text-[#64748B]">
          SIGINEX · SGI
        </p>
        <h2 className="mt-0.5 text-[19px] font-semibold text-[#14202E]">
          {titulo}
        </h2>
      </div>
      <div className="flex items-center gap-3">
        <span className="rounded-[7px] border border-[#F3E7CC] bg-[#FFF7E6] px-2.5 py-1 font-mono text-[11px] font-semibold text-[#a9781f]">
          KB {KB_VERSION}
        </span>
      </div>
    </header>
  );
}
