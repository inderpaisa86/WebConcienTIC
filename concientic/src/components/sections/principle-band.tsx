import { principleBand } from "@/content/site";

export function PrincipleBand() {
  return (
    <section className="principle-band" aria-label="Principio ConcienTIC">
      <div className="ct-container">
        <p>
          {principleBand.statement}
          <br />
          <strong>{principleBand.emphasis}</strong>
        </p>
      </div>
    </section>
  );
}
