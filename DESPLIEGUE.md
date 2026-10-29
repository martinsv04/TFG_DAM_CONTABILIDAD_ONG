# Despliegue de ONGestión

Arquitectura: **Supabase** (base de datos PostgreSQL) + **Render** (backend Spring Boot) + **Vercel** (frontend React).
Supabase solo aloja la base de datos: no puede ejecutar el backend Java ni servir el frontend, por eso se usan Render y Vercel (los tres tienen plan gratuito).

Orden: 1) Supabase → 2) subir a GitHub → 3) Render → 4) Supabase (seguridad) → 5) Vercel → 6) conectar Render con Vercel → 7) probar.

---

## 1. Supabase (base de datos)

1. Entra en https://supabase.com y crea un proyecto nuevo. Elige la región más cercana (por ejemplo, *West EU*). **Guarda la contraseña de la base de datos** que escribas aquí.
2. Cuando esté listo, pulsa **Connect** (arriba) → pestaña **Connection string** → método **Session pooler** (no «Direct connection»: la conexión directa usa IPv6 y Render solo tiene IPv4).
3. Apunta tres datos de esa pantalla:
   - **Host**, algo como `aws-0-eu-west-1.pooler.supabase.com`
   - **Usuario**: `postgres.<id-del-proyecto>` (lleva el punto y el id)
   - **Puerto**: `5432`
4. Con ellos formarás estas variables para Render:
   - `DB_URL` = `jdbc:postgresql://<HOST>:5432/postgres?sslmode=require`
   - `DB_USER` = `postgres.<id-del-proyecto>`
   - `DB_PASSWORD` = la contraseña del paso 1

No hace falta importar `tfg_db.sql`: al arrancar, el backend crea las tablas él solo (Hibernate). Empiezas con la base vacía.

## 2. Subir los cambios a GitHub

Haz commit y push de todo lo nuevo (Dockerfile, `application.properties`, `vercel.json`, etc.). Render y Vercel despliegan desde el repositorio.

Antes, comprueba en tu ordenador que el frontend compila: en `frontend`, ejecuta `npm install` (se añade `cross-env`) y `npm run build`.

## 3. Render (backend)

1. Entra en https://render.com → **New +** → **Web Service** → conecta tu cuenta de GitHub y elige el repositorio.
2. Configuración:
   - **Root Directory**: `ong`
   - **Language / Runtime**: `Docker` (usa el `Dockerfile` de esa carpeta)
   - **Instance Type**: `Free`
3. En **Environment Variables** añade:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://<HOST>:5432/postgres?sslmode=require` |
| `DB_USER` | `postgres.<id-del-proyecto>` |
| `DB_PASSWORD` | contraseña de la base de datos |
| `JWT_SECRET` | una cadena aleatoria de 32 o más caracteres (guárdala; si la cambias, se cierran las sesiones) |
| `ADMIN_EMAIL` | el correo del primer administrador |
| `ADMIN_PASSWORD` | su contraseña (mínimo 8 caracteres, con letras y números) |
| `CORS_ORIGINS` | de momento `http://localhost:3000` (se cambia en el paso 6) |
| `SWAGGER_ENABLED` | `false` |

4. Pulsa **Create Web Service**. La primera construcción tarda varios minutos.
5. En los **Logs** debes ver `Creado el administrador inicial ...` y `Started OngApplication`. La dirección del servicio será algo como `https://ongestion-api.onrender.com` (cópiala).

Si el log muestra «password authentication failed», revisa que el usuario sea `postgres.<id>` y la contraseña. Si muestra «Network is unreachable», estás usando el host directo en vez del del *pooler*.

## 4. Supabase: seguridad de las tablas (una sola vez)

Supabase publica por defecto una API sobre las tablas. Para que nadie pueda leerlas desde fuera:

1. En Supabase abre **SQL Editor** → **New query**.
2. Pega el contenido de `supabase_seguridad.sql` y ejecútalo. Al final debe listar todas las tablas con `rowsecurity = true`.

Vuelve a ejecutarlo si algún día el backend crea tablas nuevas.

## 5. Vercel (frontend)

1. Entra en https://vercel.com → **Add New… → Project** → importa el repositorio.
2. Configuración:
   - **Root Directory**: `frontend`
   - **Framework Preset**: `Create React App`
   - **Environment Variables**: `REACT_APP_API_URL` = la dirección de Render, **sin barra final** (por ejemplo `https://ongestion-api.onrender.com`)
3. **Deploy**. Al terminar tendrás una dirección como `https://ongestion.vercel.app`.

Si el build falla por la versión de Node, en *Settings → General → Node.js Version* prueba con `18.x`.

## 6. Conectar el backend con el frontend (CORS)

En Render, edita la variable `CORS_ORIGINS` y pon la dirección de Vercel completa, sin barra final (por ejemplo `https://ongestion.vercel.app`). Si tienes varias, sepáralas con comas. Guarda: Render reinicia el servicio.

## 7. Probar

1. Abre la dirección de Vercel → **Iniciar sesión** con `ADMIN_EMAIL` y `ADMIN_PASSWORD`. (La primera petición puede tardar hasta un minuto: el plan gratuito de Render se «duerme».)
2. Crea la ONG, registra un ingreso, genera su factura y abre **Registro de actividad** y **Resumen por cuenta**.
3. Cambia la contraseña del administrador en **Mi perfil**.

## 8. Stripe (cobro del plan Pro)

La aplicación funciona sin Stripe (todo el mundo tendría la prueba de 14 días y después el plan Básico), pero para cobrar el plan Pro hay que configurarlo. Empieza siempre en **modo de prueba** (interruptor «Modo de prueba» / *Test mode* del panel de Stripe): no mueve dinero real.

1. **Cuenta:** crea una cuenta en https://stripe.com (no hace falta verificar la empresa para el modo de prueba).
2. **Producto y precios:** en *Catálogo de productos → Añadir producto*: nombre «ONGestión Pro». Añade dos precios **recurrentes**: uno **mensual** y otro **anual** (tú decides los importes, en euros). Abre cada precio y copia su identificador (`price_...`).
3. **Clave secreta:** en *Desarrolladores → Claves de API*, copia la **clave secreta** de prueba (`sk_test_...`).
4. **Portal de cliente:** en *Ajustes → Facturación → Portal de cliente*, actívalo y permite cancelar la suscripción y actualizar el método de pago.
5. **Webhook:** en *Desarrolladores → Webhooks → Añadir endpoint*:
   - URL: `https://TU-BACKEND.onrender.com/api/stripe/webhook`
   - Eventos: `checkout.session.completed`, `customer.subscription.created`, `customer.subscription.updated`, `customer.subscription.deleted`
   - Al crearlo, copia el **secreto de firma** (`whsec_...`).
6. **Variables en Render** (Environment):

| Variable | Valor |
|---|---|
| `STRIPE_SECRET_KEY` | `sk_test_...` |
| `STRIPE_WEBHOOK_SECRET` | `whsec_...` |
| `STRIPE_PRICE_MENSUAL` | `price_...` del precio mensual |
| `STRIPE_PRICE_ANUAL` | `price_...` del precio anual |
| `FRONTEND_URL` | la dirección de Vercel, sin barra final |

7. **Probar:** entra como administrador → *Plan y facturación* → Suscribirme. Paga con la tarjeta de prueba `4242 4242 4242 4242`, cualquier fecha futura y cualquier CVC. Al volver, el plan pasa a Pro en unos segundos. Después prueba «Gestionar suscripción».

Para pasar a cobros reales: activa tu cuenta de Stripe con los datos y la cuenta bancaria de quien cobra, repite los pasos 2 a 5 en modo real y sustituye las claves y los `price_...` en Render. Cobrar un servicio con IVA exige estar dado de alta como autónomo o empresa.

**Tablas nuevas:** el backend crea la tabla `suscripciones` solo. Si usas Supabase, vuelve a ejecutar `supabase_seguridad.sql` para activar la seguridad por filas también en esa tabla.

## Limitaciones del plan gratuito

- **Render** apaga el servicio tras 15 minutos sin visitas; la siguiente visita tarda 30–60 segundos en responder. Conviene «despertarlo» unos minutos antes de una demostración.
- **Supabase** pausa los proyectos gratuitos tras una semana sin actividad (se reactivan desde el panel).
- Cuando tengas dominio propio, actualiza `frontend/src/config/sitio.js`, `frontend/public/sitemap.xml` y `frontend/public/robots.txt`.

## Probar en local contra Supabase (opcional)

Define las mismas variables `DB_URL`, `DB_USER` y `DB_PASSWORD` antes de arrancar el backend. Sin ellas, sigue usando tu MySQL local como hasta ahora.
