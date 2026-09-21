import { Analytics } from "@vercel/analytics/next";
import { SpeedInsights } from "@vercel/speed-insights/next";

export function VercelAnalyticsComponent() {
  return (
    <>
      <Analytics />
      <SpeedInsights />
    </>
  );
}
