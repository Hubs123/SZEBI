function decodePayload(token) {
  try {
    const part = token.split(".")[1];
    if (!part) return null;
    const base64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(base64);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getUserRole() {
  const token = sessionStorage.getItem("token");
  if (!token) return null;
  const payload = decodePayload(token);
  const role = payload?.role;
  return typeof role === "string" ? role : null;
}

export function requireAdmin() {
  const role = (getUserRole() || "").toUpperCase();
  return role === "ROLE_ADMIN" || role === "ADMIN";
}

export function requireResident() {
  const role = (getUserRole() || "").toUpperCase();
  return role === "ROLE_USER" || role === "USER" || role === "ROLE_RESIDENT" || role === "RESIDENT";
}
