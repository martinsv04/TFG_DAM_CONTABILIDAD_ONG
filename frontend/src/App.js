import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import PublicLayout from './components/PublicLayout';
import AppLayout from './components/AppLayout';
import { Toaster } from './components/Toast';
import Home from './components/Home';
import Register from './pages/Register';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import PrivateRoute from './components/PrivateRoute';
import VistaOng from './pages/ViewOng';
import EditOng from './pages/EditOng';
import AddMember from './pages/AddMember';
import EconomicArea from './pages/EconomicArea';
import AddIncome from './pages/AddIncome';
import AddExpense from './pages/AddExpense';
import EstadoResultados from './pages/EstadoResultados';
import BalanceGeneral from './pages/BalanceGeneral';
import FacturasList from './pages/Facturas';
import FacturaDetalle from './pages/FacturaDetalle';
import NuevaFactura from './pages/NuevaFactura';
import Certificados from './pages/Certificados';
import CertificadoVer from './pages/CertificadoVer';
import MisCertificados from './pages/MisCertificados';
import Modelo182 from './pages/Modelo182';
import Proyectos from './pages/Proyectos';
import ProyectoDetalle from './pages/ProyectoDetalle';
import Subvenciones from './pages/Subvenciones';
import Exportar from './pages/Exportar';
import Auditoria from './pages/Auditoria';
import ResumenCuentas from './pages/ResumenCuentas';
import Perfil from './pages/Perfil';
import NuevaOng from './pages/NuevaOng';
import Donar from './pages/Donar';
import Privacidad from './pages/Privacidad';
import Terminos from './pages/Terminos';
import Gracias from './pages/Gracias';
import NoEncontrada from './pages/NoEncontrada';
import TituloPagina from './components/TituloPagina';
import BannerCookies from './components/BannerCookies';

function App() {
  return (
    <Router>
      <TituloPagina />
      <Toaster />
      <BannerCookies />
      <Routes>
        {/* Zona pública */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/registrarse" element={<Register />} />
          <Route path="/iniciar-sesion" element={<Login />} />
          <Route path="/privacidad" element={<Privacidad />} />
          <Route path="/terminos" element={<Terminos />} />
          <Route path="/gracias" element={<Gracias />} />
        </Route>

        {/* Zona privada: barra lateral + contenido */}
        <Route element={<PrivateRoute><AppLayout /></PrivateRoute>}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/ong/:id" element={<VistaOng />} />
          <Route path="/nueva-ong" element={<PrivateRoute roles={['ADMIN']}><NuevaOng /></PrivateRoute>} />
          <Route path="/editar-ong/:id" element={<PrivateRoute roles={['ADMIN']}><EditOng /></PrivateRoute>} />
          <Route path="/add-member/:id" element={<PrivateRoute roles={['ADMIN']}><AddMember /></PrivateRoute>} />
          <Route path="/area-economica/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><EconomicArea /></PrivateRoute>} />
          <Route path="/ongs/:id/ingresos/nuevo" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><AddIncome /></PrivateRoute>} />
          <Route path="/ongs/:id/gastos/nuevo" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><AddExpense /></PrivateRoute>} />
          <Route path="/ongs/:id/ingresos/:movimientoId/editar" element={<PrivateRoute roles={['ADMIN']}><AddIncome /></PrivateRoute>} />
          <Route path="/ongs/:id/gastos/:movimientoId/editar" element={<PrivateRoute roles={['ADMIN']}><AddExpense /></PrivateRoute>} />
          <Route path="/informe/resultados/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><EstadoResultados /></PrivateRoute>} />
          <Route path="/informe/balance/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><BalanceGeneral /></PrivateRoute>} />
          <Route path="/ongs/:id/facturas" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><FacturasList /></PrivateRoute>} />
          <Route path="/ongs/:id/facturas/nueva" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><NuevaFactura /></PrivateRoute>} />
          <Route path="/ongs/:id/certificados" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><Certificados /></PrivateRoute>} />
          <Route path="/ongs/:id/certificados/donacion/:ingresoId" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><CertificadoVer /></PrivateRoute>} />
          <Route path="/ongs/:id/certificados/anual/:usuarioId" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><CertificadoVer /></PrivateRoute>} />
          <Route path="/ongs/:id/proyectos" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><Proyectos /></PrivateRoute>} />
          <Route path="/ongs/:id/proyectos/:proyectoId" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><ProyectoDetalle /></PrivateRoute>} />
          <Route path="/ongs/:id/subvenciones" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><Subvenciones /></PrivateRoute>} />
          <Route path="/ongs/:id/auditoria" element={<PrivateRoute roles={['ADMIN']}><Auditoria /></PrivateRoute>} />
          <Route path="/ongs/:id/cuentas" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><ResumenCuentas /></PrivateRoute>} />
          <Route path="/ongs/:id/exportar" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><Exportar /></PrivateRoute>} />
          <Route path="/ongs/:id/modelo-182" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><Modelo182 /></PrivateRoute>} />
          <Route path="/ongs/:id/mis-certificados" element={<PrivateRoute roles={['DONANTE']}><MisCertificados /></PrivateRoute>} />
          <Route path="/perfil" element={<Perfil />} />
          <Route path="/facturas/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><FacturaDetalle /></PrivateRoute>} />
          <Route path="/donar/:id" element={<PrivateRoute roles={['DONANTE']}><Donar /></PrivateRoute>} />
        </Route>

        {/* Cualquier otra dirección: página 404 */}
        <Route element={<PublicLayout />}>
          <Route path="*" element={<NoEncontrada />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
