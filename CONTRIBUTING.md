






Prerequisites:

- `deder` of course
- JDK 21+
- `pkl-codegen-java`



Pkl bindings should be regenerated after editing a plugin's Pkl config (e.g. `deder-protobuf/resources/ProtobufPlugin.pkl`):
```bash
./scripts/gen-plugin-bindings.sh
```
Bindings are committed in this repository.

Run tests from the repo root:

```bash
deder exec -t test -m deder-protobuf-test
```

