-- Ejecutar UNA VEZ en Supabase (SQL Editor) después de que el backend haya arrancado y creado las tablas.
-- Activa la seguridad por filas (RLS) en todas las tablas de la aplicación. Sin políticas, la API pública
-- de Supabase (con la clave "anon") no puede leer ni escribir nada. El backend se conecta como propietario
-- de las tablas, así que no le afecta.
-- Vuelve a ejecutarlo si en el futuro Hibernate crea tablas nuevas.
DO $$
DECLARE t record;
BEGIN
  FOR t IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
    EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', t.tablename);
  END LOOP;
END $$;

-- Comprobación: todas las filas deben mostrar rowsecurity = true
SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
