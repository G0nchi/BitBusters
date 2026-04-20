# BitBusters Admin - Complete Navigation Map

## Overview
This document maps all navigation flows between the 16 admin activities. Each section represents a source activity with its clickable elements and target activities.

---

## Activity 1: AdminMainActivity (Dashboard)
**BottomNav**: Visible ✅  
**Clickable Elements:**
- "Crear proyecto" button → **AdminCrearProyectoActivity** (Activity 14)
- "Ver detalles de..." cards → **AdminDetallesProyectoActivity** (Activity 10) 
- Top-right notification icon → **AdminNotificacionesActivity** (Activity 6)
- Notification bell → **AdminNotificacionesActivity**
- Avatar icon → **AdminPerfilActivity** (Activity 5)

---

## Activity 2: AdminProyectosActivity (Projects List)
**BottomNav**: Visible ✅  
**Clickable Elements:**
- "+ Crear proyecto" button → **AdminCrearProyectoActivity** (Activity 14)
- Project cards (4 total) → **AdminDetallesProyectoActivity** (Activity 10)
- Notification icon → **AdminNotificacionesActivity** (Activity 6)
- Avatar icon → **AdminPerfilActivity** (Activity 5)

---

## Activity 3: AdminSeparacionesActivity (Reservations/Separations)
**BottomNav**: Visible ✅  
**Clickable Elements:**
- Separation cards (3+ items) → **AdminDetallesSeparacionActivity** (Activity 15)
- Filter chips (Pendientes/Aprobadas/Rechazadas) → Filter state only, no navigation
- Notification icon → **AdminNotificacionesActivity** (Activity 6)
- Avatar icon → **AdminPerfilActivity** (Activity 5)

---

## Activity 4: AdminReportesActivity (Analytics/Reports)
**BottomNav**: Visible ✅  
**Clickable Elements:**
- Notification icon → **AdminNotificacionesActivity** (Activity 6)
- Avatar icon → **AdminPerfilActivity** (Activity 5)
- Report cards → **AdminDetallesDeReporteProyectoActivity** (Activity 16)

---

## Activity 5: AdminPerfilActivity (Profile/Company Info)
**BottomNav**: Accessible via menu but NOT visible inside  
**Clickable Elements:**
- "Editar Inmobiliaria" button → **AdminEditarInmobiliariaActivity** (Activity 8)
- Notification icon → **AdminNotificacionesActivity** (Activity 6)
- Advisor rows → Show advisor details or **AdminDetallesInmobiliariaActivity** (Activity 7)
- Back/Notification ImageButton → **AdminNotificacionesActivity** (Activity 6)

---

## Activity 6: AdminNotificacionesActivity (Notifications)
**BottomNav**: NO  
**Clickable Elements:**
- Back arrow (ImageButton) → Back/finish() - Return to calling activity
- Notification items → Could navigate to related content or stay on screen
- No BottomNav

---

## Activity 7: AdminDetallesInmobiliariaActivity (Company Details - Read Only)
**BottomNav**: NO  
**Clickable Elements:**
- "Editar Inmobiliaria" button → **AdminEditarInmobiliariaActivity** (Activity 8)
- Back arrow → Back/finish()
- No BottomNav

---

## Activity 8: AdminEditarInmobiliariaActivity (Edit Company Info)
**BottomNav**: NO  
**Clickable Elements:**
- "+ Agregar asesor" button → **AdminRegistrarAsesorActivity** (Activity 9)
- "+ Agregar foto" button → Image picker or photo management (could stay on form)
- "Guardar" button → Save and finish() or navigate back
- "Cancelar" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Activity 9: AdminRegistrarAsesorActivity (Register Advisor)
**BottomNav**: NO  
**Clickable Elements:**
- "Registrar" button → Save and finish() (returns to EditarInmobiliaria)
- "Cancelar" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Activity 10: AdminDetallesProyectoActivity (Project Details)
**BottomNav**: NO  
**Clickable Elements:**
- "Editar proyecto" button → **AdminEditarProyectoActivity** (Activity 11)
- "Ver detalles de reporte" button → **AdminDetallesDeReporteProyectoActivity** (Activity 16)
- "Volver" button → Back/finish()
- Back arrow (ImageButton) → finish()
- No BottomNav

---

## Activity 11: AdminEditarProyectoActivity (Edit Project)
**BottomNav**: NO  
**Clickable Elements:**
- "+ Agregar tipología" button → **AdminAgregarTipologiaActivity** (Activity 12)
- "Guardar" button → Save and finish()
- "Cancelar" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Activity 12: AdminAgregarTipologiaActivity (Add Typology)
**BottomNav**: NO  
**Clickable Elements:**
- "Agregar" button → Save typology and finish()
- "Cancelar" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Activity 13: AdminAsignarAsesoresActivity (Assign Advisors)
**BottomNav**: NO  
**Clickable Elements:**
- Checkboxes → State management only (no navigation)
- "Guardar" button → Save assignments and finish()
- "Cancelar" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Activity 14: AdminCrearProyectoActivity (Create Project)
**BottomNav**: NO  
**Clickable Elements:**
- Multi-step form navigation (internal only)
- "+ Agregar tipología" button → **AdminAgregarTipologiaActivity** (Activity 12)
- "+ Asignar asesores" button → **AdminAsignarAsesoresActivity** (Activity 13)
- "Crear proyecto" final button → Save and return to Projects list OR Dashboard
- "Cancelar" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Activity 15: AdminDetallesSeparacionActivity (Separation/Reservation Details)
**BottomNav**: NO  
**Clickable Elements:**
- "Aprobar" button → Save status and finish()
- "Rechazar" button → Show confirmation and finish()
- "Editar" button → Could go to edit form
- Back arrow → finish()
- "Volver" button → finish()
- No BottomNav

---

## Activity 16: AdminDetallesDeReporteProyectoActivity (Project Report Details)
**BottomNav**: NO  
**Clickable Elements:**
- "Descargar reporte" button → Download PDF or show toast (no navigation)
- "Volver" button → finish()
- Back arrow → finish()
- No BottomNav

---

## Summary - Navigation Targets Count
- **AdminMainActivity (Dashboard)**: 5 outbound flows (Crear Proyecto, Proyectos cards, Notificaciones, Perfil)
- **AdminProyectosActivity**: 3 outbound flows
- **AdminSeparacionesActivity**: 2 outbound flows
- **AdminReportesActivity**: 2 outbound flows
- **AdminPerfilActivity**: 3 outbound flows
- **AdminNotificacionesActivity**: 1 inbound, back navigation
- **AdminDetallesInmobiliariaActivity**: 1 outbound + back
- **AdminEditarInmobiliariaActivity**: 2 outbound + back
- **AdminRegistrarAsesorActivity**: back navigation only
- **AdminDetallesProyectoActivity**: 2 outbound + back
- **AdminEditarProyectoActivity**: 1 outbound + back
- **AdminAgregarTipologiaActivity**: back navigation only
- **AdminAsignarAsesoresActivity**: back navigation only
- **AdminCrearProyectoActivity**: 2 outbound + back
- **AdminDetallesSeparacionActivity**: back navigation only
- **AdminDetallesDeReporteProyectoActivity**: back navigation only

---

## Implementation Priority
1. **Phase 1 (Core Flows)**: Dashboard → Proyectos → DetallesProyecto → EditarProyecto
2. **Phase 2 (Secondary Flows)**: Separaciones → DetallesSeparacion, Perfil → EditarInmobiliaria
3. **Phase 3 (Creation Flows)**: CrearProyecto with nested forms
4. **Phase 4 (Detail Flows)**: All remaining transitions

