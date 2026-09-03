"use client";

import Image from "next/image";
import { Menu, X } from "lucide-react";
import { useState } from "react";

import { navLinks, site } from "@/content/site";

export function Header() {
  const [open, setOpen] = useState(false);

  return (
    <header className="site-header">
      <div className="ct-container header-inner">
        <a className="brand-link" href="#inicio" aria-label={`${site.name}, inicio`}>
          <Image
            src="/brand/logo-concientic.png"
            alt="ConcienTIC"
            width={316}
            height={121}
            priority
          />
        </a>

        <nav className="site-nav" aria-label="Principal">
          {navLinks.map((link) => (
            <a key={link.href} href={link.href}>
              {link.label}
            </a>
          ))}
        </nav>

        <div className="header-actions">
          <a className="ct-button ct-button--dark" href="#contacto">
            Hablemos
          </a>
          <button
            className="mobile-menu-button"
            type="button"
            aria-label={open ? "Cerrar menú" : "Abrir menú"}
            aria-expanded={open}
            aria-controls="mobile-nav"
            onClick={() => setOpen((value) => !value)}
          >
            {open ? <X size={24} aria-hidden="true" /> : <Menu size={24} aria-hidden="true" />}
          </button>
        </div>
      </div>

      <nav id="mobile-nav" className="mobile-nav" data-open={open} aria-label="Principal móvil">
        <div className="ct-container">
          <ul>
            {navLinks.map((link) => (
              <li key={link.href}>
                <a href={link.href} onClick={() => setOpen(false)}>
                  {link.label}
                </a>
              </li>
            ))}
            <li>
              <a href="#contacto" onClick={() => setOpen(false)}>
                Hablemos
              </a>
            </li>
          </ul>
        </div>
      </nav>
    </header>
  );
}
