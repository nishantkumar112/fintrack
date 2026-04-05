const normalizeRoleValue = (value) => {
  if (value == null) return "EMPLOYEE";
  if (typeof value === "string") {
    return value.replace(/^ROLE_/i, "").toUpperCase();
  }
  return "EMPLOYEE";
};

export const parseJwt = (token) => {
  if (!token || typeof token !== "string") return {};
  try {
    const payload = token.split(".")[1];
    if (!payload) return {};
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return {};
  }
};

export const roleFromClaims = (claims) => {
  if (!claims || typeof claims !== "object") return "EMPLOYEE";
  if (claims.role) return normalizeRoleValue(claims.role);
  const auth = claims.authorities ?? claims.roles ?? claims.scope;
  if (Array.isArray(auth) && auth.length > 0) {
    const first = auth[0];
    if (typeof first === "string") return normalizeRoleValue(first);
    if (first?.authority) return normalizeRoleValue(first.authority);
  }
  if (typeof auth === "string") return normalizeRoleValue(auth);
  return "EMPLOYEE";
};

export const userFromClaims = (claims) => {
  if (!claims || typeof claims !== "object") return null;
  return {
    id: claims.userId ?? claims.id ?? claims.sub,
    email: claims.email ?? claims.sub,
    firstName: claims.firstName ?? claims.given_name,
    lastName: claims.lastName ?? claims.family_name,
    role: roleFromClaims(claims),
  };
};

export const normalizeAuthPayload = (data) => {
  const token = data?.accessToken ?? data?.token ?? data?.jwt;
  let user = data?.user ?? null;
  if (!user && token) {
    user = userFromClaims(parseJwt(token));
  }
  if (user && !user.role) {
    user = { ...user, role: roleFromClaims(parseJwt(token)) };
  }
  return { token, user };
};
