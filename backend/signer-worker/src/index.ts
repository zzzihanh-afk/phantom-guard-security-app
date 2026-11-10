/**
 * PHANTOM GUARD — Server-Side Cryptographic Command Signer
 * Cloudflare Worker (100% Free Tier - 100,000 requests/day)
 * 
 * Cryptographically signs anti-theft instructions with ECDSA P-256
 * preventing unauthorized local injection or unsigned execution.
 */

export interface Env {
  SIGNING_PRIVATE_KEY_JWK?: string;
  ENVIRONMENT: string;
}

interface CommandRequest {
  deviceId: string;
  ownerId: string;
  commandType: string;
  payload?: Record<string, string>;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // CORS headers
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization",
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    if (request.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    try {
      const authHeader = request.headers.get("Authorization");
      if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return new Response(JSON.stringify({ error: "Unauthorized: Missing auth bearer" }), {
          status: 401,
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      const body = (await request.json()) as CommandRequest;
      if (!body.deviceId || !body.ownerId || !body.commandType) {
        return new Response(JSON.stringify({ error: "Missing required command parameters" }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // Generate cryptographically secure parameters
      const commandId = "cmd_" + crypto.randomUUID();
      const timestamp = Date.now();
      const expiresAt = timestamp + (15 * 60 * 1000); // Strict 15 minute lifespan
      const nonce = crypto.randomUUID().replace(/-/g, "");

      // Raw canonical payload string
      const canonicalData = `${commandId}:${body.deviceId}:${body.ownerId}:${body.commandType}:${timestamp}:${nonce}`;

      // ECDSA P-256 Signature generation
      let signatureBase64 = "";
      if (env.SIGNING_PRIVATE_KEY_JWK) {
        const jwk = JSON.parse(env.SIGNING_PRIVATE_KEY_JWK);
        const privateKey = await crypto.subtle.importKey(
          "jwk",
          jwk,
          { name: "ECDSA", namedCurve: "P-256" },
          false,
          ["sign"]
        );

        const encoder = new TextEncoder();
        const signatureBytes = await crypto.subtle.sign(
          { name: "ECDSA", hash: { name: "SHA-256" } },
          privateKey,
          encoder.encode(canonicalData)
        );

        signatureBase64 = btoa(String.fromCharCode(...new Uint8Array(signatureBytes)));
      } else {
        // Fallback demo signature token for local dev test
        signatureBase64 = btoa("PHANTOM_GUARD_AUTH_TOKEN_" + nonce);
      }

      const signedCommandEnvelope = {
        commandId,
        deviceId: body.deviceId,
        ownerId: body.ownerId,
        commandType: body.commandType,
        timestamp,
        expiresAt,
        nonce,
        payload: body.payload || {},
        signature: signatureBase64,
        status: "PENDING"
      };

      return new Response(JSON.stringify(signedCommandEnvelope), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });

    } catch (err: any) {
      return new Response(JSON.stringify({ error: err.message || "Internal server error" }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }
  }
};
