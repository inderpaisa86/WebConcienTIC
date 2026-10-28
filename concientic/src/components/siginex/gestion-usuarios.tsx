"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  cambiarActivoUsuario,
  crearUsuario,
  isSiginexDataError,
  listarUsuarios,
  type UsuarioResumen,
} from "@/lib/siginex";
import {
  siginexUsuarioSchema,
  type SiginexUsuarioInput,
} from "@/lib/validations/siginex-usuario";

type EstadoCarga = "cargando" | "listo" | "error";

const SELECT_CLASSES =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm text-foreground transition-colors outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50";

/**
 * Gestión de usuarios por el admin (Fase 5, "use client"). Lista los usuarios
 * del tenant, permite crear uno nuevo y activar/desactivar. Patrón client
 * coherente con `gestion-diagnosticos` (SessionGuard client + carga vía
 * `lib/siginex`, sin server actions). Un 401 redirige a login; un 403 (no
 * admin) se muestra como error de autorización.
 */
export function GestionUsuarios() {
  const router = useRouter();
  const [usuarios, setUsuarios] = useState<UsuarioResumen[]>([]);
  const [estado, setEstado] = useState<EstadoCarga>("cargando");
  const [mensajeError, setMensajeError] = useState<string | null>(null);
  const [accionId, setAccionId] = useState<string | null>(null);

  const resolverError = useCallback(
    (err: unknown): string => {
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        router.replace("/siginex/login");
        return "Tu sesión no es válida o ha caducado.";
      }
      return isSiginexDataError(err)
        ? err.message
        : "Ha ocurrido un error inesperado. Inténtalo de nuevo.";
    },
    [router],
  );

  const cargar = useCallback(async () => {
    setEstado("cargando");
    setMensajeError(null);
    try {
      const datos = await listarUsuarios();
      setUsuarios(datos);
      setEstado("listo");
    } catch (err) {
      setMensajeError(resolverError(err));
      setEstado("error");
    }
  }, [resolverError]);

  useEffect(() => {
    let activo = true;
    listarUsuarios()
      .then((datos) => {
        if (!activo) return;
        setUsuarios(datos);
        setEstado("listo");
      })
      .catch((err: unknown) => {
        if (!activo) return;
        setMensajeError(resolverError(err));
        setEstado("error");
      });
    return () => {
      activo = false;
    };
  }, [resolverError]);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SiginexUsuarioInput>({
    resolver: zodResolver(siginexUsuarioSchema),
    defaultValues: { email: "", nombre: "", password: "", rol: "miembro" },
  });

  const [formError, setFormError] = useState<string | null>(null);

  async function onCrear(values: SiginexUsuarioInput) {
    setFormError(null);
    try {
      await crearUsuario(values);
      reset({ email: "", nombre: "", password: "", rol: "miembro" });
      void cargar();
    } catch (err) {
      setFormError(resolverError(err));
    }
  }

  async function alternarActivo(u: UsuarioResumen) {
    setAccionId(u.id);
    setMensajeError(null);
    try {
      await cambiarActivoUsuario(u.id, !u.activo);
      void cargar();
    } catch (err) {
      setMensajeError(resolverError(err));
    } finally {
      setAccionId(null);
    }
  }

  return (
    <div className="mx-auto w-full max-w-5xl space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-heading text-2xl font-semibold text-foreground">
            Usuarios de la empresa
          </h1>
          <p className="text-sm text-muted-foreground">
            Crea usuarios de tu empresa y activa o desactiva su acceso.
          </p>
        </div>
        <Button variant="outline" size="lg" render={<Link href="/siginex" />}>
          Volver al panel
        </Button>
      </header>

      {/* Crear usuario */}
      <Card>
        <CardHeader>
          <CardTitle>Nuevo usuario</CardTitle>
          <CardDescription>
            El usuario se crea en tu empresa. La contraseña debe tener al menos
            12 caracteres.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleSubmit(onCrear)}
            noValidate
            className="grid gap-4 sm:grid-cols-2"
          >
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email">Correo</Label>
              <Input
                id="email"
                type="email"
                autoComplete="off"
                aria-invalid={!!errors.email}
                {...register("email")}
              />
              {errors.email?.message ? (
                <p className="text-sm text-destructive">{errors.email.message}</p>
              ) : null}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="nombre">Nombre</Label>
              <Input
                id="nombre"
                aria-invalid={!!errors.nombre}
                {...register("nombre")}
              />
              {errors.nombre?.message ? (
                <p className="text-sm text-destructive">{errors.nombre.message}</p>
              ) : null}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password">Contraseña</Label>
              <Input
                id="password"
                type="password"
                autoComplete="new-password"
                aria-invalid={!!errors.password}
                {...register("password")}
              />
              {errors.password?.message ? (
                <p className="text-sm text-destructive">
                  {errors.password.message}
                </p>
              ) : null}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="rol">Rol</Label>
              <select
                id="rol"
                className={SELECT_CLASSES}
                aria-invalid={!!errors.rol}
                {...register("rol")}
              >
                <option value="miembro">Miembro</option>
                <option value="admin">Administrador</option>
              </select>
              {errors.rol?.message ? (
                <p className="text-sm text-destructive">{errors.rol.message}</p>
              ) : null}
            </div>

            {formError ? (
              <p
                role="alert"
                aria-live="assertive"
                className="text-sm text-destructive sm:col-span-2"
              >
                {formError}
              </p>
            ) : null}

            <div className="sm:col-span-2">
              <Button type="submit" size="lg" disabled={isSubmitting}>
                {isSubmitting ? "Creando…" : "Crear usuario"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Lista de usuarios */}
      <section className="space-y-3">
        <h2 className="font-heading text-lg font-semibold text-foreground">
          Usuarios
        </h2>

        {estado === "cargando" ? (
          <p role="status" aria-live="polite" className="text-sm text-muted-foreground">
            Cargando usuarios…
          </p>
        ) : null}

        {estado === "error" ? (
          <div
            role="status"
            aria-live="polite"
            className="flex flex-col items-start gap-3 rounded-xl border border-border bg-card p-6"
          >
            <p className="text-sm text-muted-foreground">
              {mensajeError ?? "No se pudieron cargar los usuarios."}
            </p>
            <Button variant="outline" size="sm" onClick={() => void cargar()}>
              Reintentar
            </Button>
          </div>
        ) : null}

        {estado === "listo" ? (
          usuarios.length === 0 ? (
            <p className="rounded-xl border border-dashed border-border bg-card p-6 text-center text-sm text-muted-foreground">
              Aún no hay usuarios. Crea el primero arriba.
            </p>
          ) : (
            <div className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left">
                    <th scope="col" className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                      Nombre
                    </th>
                    <th scope="col" className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                      Correo
                    </th>
                    <th scope="col" className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                      Rol
                    </th>
                    <th scope="col" className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                      Estado
                    </th>
                    <th scope="col" className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                      <span className="sr-only">Acciones</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map((u) => (
                    <tr key={u.id} className="border-b border-border last:border-0">
                      <td className="px-4 py-3 font-medium text-foreground">{u.nombre}</td>
                      <td className="px-4 py-3 text-foreground">{u.email}</td>
                      <td className="px-4 py-3">
                        <Badge variant={u.rol === "admin" ? "accent" : "muted"}>
                          {u.rol}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={u.activo ? "accent" : "muted"}>
                          {u.activo ? "activo" : "inactivo"}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          disabled={accionId === u.id}
                          onClick={() => void alternarActivo(u)}
                        >
                          {accionId === u.id
                            ? "Guardando…"
                            : u.activo
                              ? "Desactivar"
                              : "Activar"}
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        ) : null}
      </section>
    </div>
  );
}
