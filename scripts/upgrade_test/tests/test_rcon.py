import socket
import struct
import threading
import unittest

from scripts.upgrade_test.rcon import Rcon, RconAuthError

AUTH = 3
EXEC = 2
RESPONSE = 0


class _Closed(Exception):
    pass


def _read_packet(conn):
    # Like vanilla RconClient: one recv() per packet, and the connection is
    # dropped when that read does not hold exactly one packet (e.g. a client
    # that pipelines two packets back to back).
    data = conn.recv(1460)
    if len(data) < 10:
        raise _Closed()
    (length,) = struct.unpack("<i", data[:4])
    if length != len(data) - 4:
        raise _Closed()
    req_id, ptype = struct.unpack("<ii", data[4:12])
    return req_id, ptype, data[12:-2].decode("utf-8")


def _packet(req_id, ptype, payload):
    data = struct.pack("<ii", req_id, ptype) + payload.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(data)) + data


class FakeRconServer:
    """Answers like a vanilla server: an auth reply, then one or more response packets per command."""

    def __init__(self, replies, password="pw", max_chunk=4096):
        self.replies = replies
        self.password = password
        self.max_chunk = max_chunk
        self.sock = socket.socket()
        self.sock.bind(("127.0.0.1", 0))
        self.sock.listen(1)
        self.port = self.sock.getsockname()[1]
        self.thread = threading.Thread(target=self._serve, daemon=True)
        self.thread.start()

    def _serve(self):
        conn, _ = self.sock.accept()
        with conn:
            try:
                req_id, _, payload = _read_packet(conn)
            except _Closed:
                return
            conn.sendall(_packet(req_id if payload == self.password else -1, EXEC, ""))
            try:
                while True:
                    req_id, _, payload = _read_packet(conn)
                    reply = self.replies.get(payload, "")
                    chunks = [reply[i:i + self.max_chunk] for i in range(0, len(reply), self.max_chunk)] or [""]
                    for chunk in chunks:
                        conn.sendall(_packet(req_id, RESPONSE, chunk))
            except (ConnectionError, struct.error, _Closed):
                pass

    def close(self):
        self.sock.close()


class RconTest(unittest.TestCase):
    def test_command_roundtrip(self):
        server = FakeRconServer({"list": "There are 0 of a max of 20 players online: "})
        try:
            with Rcon("127.0.0.1", server.port, "pw") as rcon:
                self.assertEqual(rcon.command("list"), "There are 0 of a max of 20 players online: ")
        finally:
            server.close()

    def test_multipacket_response(self):
        text = "x" * 5000
        server = FakeRconServer({"big": text})
        try:
            with Rcon("127.0.0.1", server.port, "pw") as rcon:
                self.assertEqual(rcon.command("big"), text)
                # The connection stays usable after a split reply.
                self.assertEqual(rcon.command("unknown"), "")
        finally:
            server.close()

    def test_consecutive_commands(self):
        server = FakeRconServer({"a": "1", "b": "2"})
        try:
            with Rcon("127.0.0.1", server.port, "pw") as rcon:
                self.assertEqual([rcon.command("a"), rcon.command("b"), rcon.command("a")], ["1", "2", "1"])
        finally:
            server.close()

    def test_auth_failure_raises(self):
        server = FakeRconServer({}, password="other")
        try:
            with self.assertRaises(RconAuthError):
                with Rcon("127.0.0.1", server.port, "pw"):
                    pass
        finally:
            server.close()


if __name__ == "__main__":
    unittest.main()
