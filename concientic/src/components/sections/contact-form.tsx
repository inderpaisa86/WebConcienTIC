"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { trackLeadSubmitted } from "@/lib/analytics";
import { contactSchema, type ContactInput } from "@/lib/validations/contact";

type Status = "idle" | "submitting" | "success" | "error";

export function ContactForm() {
  const [status, setStatus] = useState<Status>("idle");
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ContactInput>({
    resolver: zodResolver(contactSchema),
    defaultValues: { name: "", email: "", company: "", message: "", website: "" },
  });

  async function onSubmit(values: ContactInput) {
    setStatus("submitting");
    setServerError(null);
    try {
      const res = await fetch("/api/contact", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values),
      });
      if (!res.ok) {
        const data = (await res.json().catch(() => null)) as { error?: string } | null;
        throw new Error(data?.error ?? "No se pudo enviar el mensaje.");
      }
      setStatus("success");
      trackLeadSubmitted();
      reset();
    } catch (err) {
      setStatus("error");
      setServerError(err instanceof Error ? err.message : "Error inesperado.");
    }
  }

  if (status === "success") {
    return (
      <div
        role="status"
        className="rounded-xl border border-border bg-card p-6 text-card-foreground"
      >
        <p className="font-heading text-lg font-semibold">¡Gracias por escribirnos!</p>
        <p className="mt-1 text-sm text-muted-foreground">
          Hemos recibido tu mensaje y te responderemos pronto.
        </p>
        <Button className="mt-4" variant="outline" onClick={() => setStatus("idle")}>
          Enviar otro mensaje
        </Button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-4">
      <Field label="Nombre" htmlFor="name" error={errors.name?.message}>
        <input
          id="name"
          type="text"
          autoComplete="name"
          aria-invalid={!!errors.name}
          className={inputClass(!!errors.name)}
          {...register("name")}
        />
      </Field>

      <Field label="Correo" htmlFor="email" error={errors.email?.message}>
        <input
          id="email"
          type="email"
          autoComplete="email"
          aria-invalid={!!errors.email}
          className={inputClass(!!errors.email)}
          {...register("email")}
        />
      </Field>

      <Field label="Empresa (opcional)" htmlFor="company" error={errors.company?.message}>
        <input
          id="company"
          type="text"
          autoComplete="organization"
          aria-invalid={!!errors.company}
          className={inputClass(!!errors.company)}
          {...register("company")}
        />
      </Field>

      <Field label="Mensaje" htmlFor="message" error={errors.message?.message}>
        <textarea
          id="message"
          rows={5}
          aria-invalid={!!errors.message}
          className={inputClass(!!errors.message)}
          {...register("message")}
        />
      </Field>

      {/* Honeypot anti-spam: oculto para usuarios, visible para bots. */}
      <div aria-hidden className="absolute left-[-9999px]" tabIndex={-1}>
        <label htmlFor="website">No rellenar</label>
        <input id="website" type="text" tabIndex={-1} autoComplete="off" {...register("website")} />
      </div>

      <div className="flex items-start gap-2">
        <input
          id="consent"
          type="checkbox"
          aria-invalid={!!errors.consent}
          className="mt-1 size-4 rounded border-border"
          {...register("consent")}
        />
        <label htmlFor="consent" className="text-sm text-muted-foreground">
          Acepto la política de tratamiento de datos.
        </label>
      </div>
      {errors.consent?.message ? (
        <p className="text-sm text-destructive">{errors.consent.message}</p>
      ) : null}

      {serverError ? (
        <p role="alert" className="text-sm text-destructive">
          {serverError}
        </p>
      ) : null}

      <Button type="submit" size="lg" disabled={status === "submitting"} className="w-fit">
        {status === "submitting" ? "Enviando..." : "Enviar mensaje"}
      </Button>
    </form>
  );
}

function Field({
  label,
  htmlFor,
  error,
  children,
}: {
  label: string;
  htmlFor: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={htmlFor} className="text-sm font-medium">
        {label}
      </label>
      {children}
      {error ? <p className="text-sm text-destructive">{error}</p> : null}
    </div>
  );
}

function inputClass(hasError: boolean): string {
  return cn(
    "w-full rounded-md border bg-background px-3 py-2 text-sm outline-none transition-colors",
    "focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
    hasError ? "border-destructive" : "border-border",
  );
}
