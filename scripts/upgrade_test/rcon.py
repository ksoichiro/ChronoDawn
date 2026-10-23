"""Minimal Source RCON client for talking to a Minecraft dedicated server."""

import socket
import struct

_AUTH = 3
_EXEC = 2
_RESPONSE = 0


class RconError(Exception):
    pass


class RconAuthError(RconError):
    pass


class Rcon:
    def __init__(self, host: str, port: int, password: str, timeout: float = 30.0):
        self.host = host
        self.port = port
        self.password = password
        self.timeout = timeout
        self._sock = None
        self._next_id = 1

    def __enter__(self):
        self._sock = socket.create_connection((self.host, self.port), timeout=self.timeout)
        req_id = self._send(_AUTH, self.password)
        reply_id, _, _ = self._recv()
        if reply_id == -1 or reply_id != req_id:
            self.close()
            raise RconAuthError(f"RCON authentication failed on {self.host}:{self.port}")
        return self

    def __exit__(self, *exc):
        self.close()

    def close(self):
        if self._sock is not None:
            self._sock.close()
            self._sock = None

    def command(self, cmd: str) -> str:
        cmd_id = self._send(_EXEC, cmd)
        # Vanilla splits long replies over several packets without marking the
        # last one. A follow-up packet with an unknown type is answered only
        # after the whole reply, so its echo marks the end. It must not be sent
        # until the first reply packet arrives: vanilla reads one packet per
        # socket read and drops the connection when two arrive together.
        parts = []
        end_id = None
        while True:
            reply_id, _, payload = self._recv()
            if end_id is not None and reply_id == end_id:
                return "".join(parts)
            if reply_id == cmd_id:
                parts.append(payload)
                if end_id is None:
                    end_id = self._send(_RESPONSE, "")

    def _send(self, ptype: int, payload: str) -> int:
        req_id = self._next_id
        self._next_id += 1
        body = struct.pack("<ii", req_id, ptype) + payload.encode("utf-8") + b"\x00\x00"
        self._sock.sendall(struct.pack("<i", len(body)) + body)
        return req_id

    def _recv(self):
        (length,) = struct.unpack("<i", self._read_exact(4))
        body = self._read_exact(length)
        req_id, ptype = struct.unpack("<ii", body[:8])
        return req_id, ptype, body[8:-2].decode("utf-8")

    def _read_exact(self, n: int) -> bytes:
        data = b""
        while len(data) < n:
            chunk = self._sock.recv(n - len(data))
            if not chunk:
                raise RconError("RCON connection closed")
            data += chunk
        return data
