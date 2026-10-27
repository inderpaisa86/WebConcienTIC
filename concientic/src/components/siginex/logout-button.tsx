"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

import { Button } from "@/components/ui/button";
import { logout } from "@/lib/siginex";

/** Botón de cierre de sesión: llama a `logout()` y vuelve al login. */
export function LogoutButton() {
  const router = useRouter();
  const [pending, setPending] = useState(false);

  async function onClick() {
    setPending(true);
    await logout();
    router.replace("/siginex/login");
  }

  return (
    <Button variant="outline" size="lg" disabled={pending} onClick={onClick}>
      {pending ? "Cerrando sesión…" : "Cerrar sesión"}
    </Button>
  );
}
