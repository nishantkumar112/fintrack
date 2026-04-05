export const ROLES = {
  ADMIN: "ADMIN",
  MANAGER: "MANAGER",
  EMPLOYEE: "EMPLOYEE",
};

export const normalizeRole = (role) =>
  String(role || ROLES.EMPLOYEE)
    .replace(/^ROLE_/i, "")
    .toUpperCase();

export const isAdmin = (user) => normalizeRole(user?.role) === ROLES.ADMIN;

export const isManagerOrAdmin = (user) => {
  const r = normalizeRole(user?.role);
  return r === ROLES.ADMIN || r === ROLES.MANAGER;
};

export const canApproveTransactions = (user) => isManagerOrAdmin(user);

export const userHasAnyRole = (user, allowedRoles = []) => {
  if (!allowedRoles.length) return true;
  const r = normalizeRole(user?.role);
  return allowedRoles.map(normalizeRole).includes(r);
};
