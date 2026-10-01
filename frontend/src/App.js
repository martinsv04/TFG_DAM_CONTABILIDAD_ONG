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
import Donar from './pages/Donar';

function App() {
  return (
    <Router>
      <Toaster />
      <Routes>
        {/* Zona pública */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/registrarse" element={<Register />} />
          <Route path="/iniciar-sesion" element={<Login />} />
        </Route>

        {/* Zona privada: barra lateral + contenido */}
        <Route element={<PrivateRoute><AppLayout /></PrivateRoute>}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/ong/:id" element={<VistaOng />} />
          <Route path="/editar-ong/:id" element={<PrivateRoute roles={['ADMIN']}><EditOng /></PrivateRoute>} />
          <Route path="/add-member/:id" element={<PrivateRoute roles={['ADMIN']}><AddMember /></PrivateRoute>} />
          <Route path="/area-economica/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><EconomicArea /></PrivateRoute>} />
          <Route path="/ongs/:id/ingresos/nuevo" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><AddIncome /></PrivateRoute>} />
          <Route path="/ongs/:id/gastos/nuevo" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><AddExpense /></PrivateRoute>} />
          <Route path="/informe/resultados/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><EstadoResultados /></PrivateRoute>} />
          <Route path="/informe/balance/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><BalanceGeneral /></PrivateRoute>} />
          <Route path="/ongs/:id/facturas" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><FacturasList /></PrivateRoute>} />
          <Route path="/ongs/:id/facturas/nueva" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><NuevaFactura /></PrivateRoute>} />
          <Route path="/facturas/:id" element={<PrivateRoute roles={['ADMIN', 'CONTABLE']}><FacturaDetalle /></PrivateRoute>} />
          <Route path="/donar/:id" element={<PrivateRoute roles={['DONANTE']}><Donar /></PrivateRoute>} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
