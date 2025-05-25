import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'; 
import Navbar from './components/Navbar';
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



function App() {
  return (
    <Router>
      <div className="App">
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/registrarse" element={<Register />} />
          <Route path="/iniciar-sesion" element={<Login />} />
          <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
          <Route path="/ong/:id" element={<PrivateRoute><VistaOng /></PrivateRoute>} />
          <Route path="/editar-ong/:id" element={<EditOng />} />
          <Route path="/add-member/:id" element={<AddMember />} />
          <Route path="/area-economica/:id" element={<EconomicArea />} />
          <Route path="/ongs/:id/ingresos/nuevo" element={<AddIncome />} />
          <Route path="/ongs/:id/gastos/nuevo" element={<AddExpense />} />
          <Route path="/informe/resultados/:id" element={<EstadoResultados />} />
          <Route path="/informe/balance/:id" element={<BalanceGeneral />} />
          <Route path="/ongs/:id/facturas" element={<FacturasList />} />
          <Route path="/facturas/:id" element={<FacturaDetalle />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
