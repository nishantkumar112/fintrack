import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Loader from "./Loader";
import { userHasAnyRole } from "../utils/rbac";

const ProtectedRoute = ({ children, roles }) => {
  const { isAuthenticated, bootstrapping, user } = useAuth();
  const location = useLocation();

  if (bootstrapping) {
    return <Loader fullPage label="Restoring session" />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles?.length && !userHasAnyRole(user, roles)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default ProtectedRoute;
