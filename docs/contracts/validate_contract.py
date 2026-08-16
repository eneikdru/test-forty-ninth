#!/usr/bin/env python3
import sys
import yaml

def validate_openapi_contract(filepath: str):
    print(f"Validating OpenAPI specification: {filepath}")
    with open(filepath, 'r', encoding='utf-8') as f:
        spec = yaml.safe_load(f)

    # Basic OpenAPI 3 structure checks
    assert "openapi" in spec, "Missing 'openapi' key"
    assert spec["openapi"].startswith("3."), f"Unsupported OpenAPI version: {spec['openapi']}"
    assert "info" in spec, "Missing 'info' section"
    assert "paths" in spec, "Missing 'paths' section"
    assert "components" in spec, "Missing 'components' section"
    assert "schemas" in spec["components"], "Missing 'schemas' in components"

    # Search endpoint check
    paths = spec["paths"]
    assert "/materials/search" in paths, "Missing '/materials/search' path"
    search_get = paths["/materials/search"].get("get")
    assert search_get is not None, "Missing GET method on '/materials/search'"

    param_names = [p["name"] for p in search_get.get("parameters", [])]
    expected_params = ["q", "category", "tags", "page", "size", "sortBy", "sortOrder"]
    for param in expected_params:
        assert param in param_names, f"Missing query parameter '{param}' in search endpoint"

    # Search responses check
    responses = search_get.get("responses", {})
    assert "200" in responses, "Missing 200 response for search endpoint"
    assert "400" in responses, "Missing 400 response for search endpoint"
    assert "500" in responses, "Missing 500 response for search endpoint"

    # Material detail endpoint check
    assert "/materials/{id}" in paths, "Missing '/materials/{id}' path"
    detail_get = paths["/materials/{id}"].get("get")
    assert detail_get is not None, "Missing GET method on '/materials/{id}'"
    detail_responses = detail_get.get("responses", {})
    assert "200" in detail_responses, "Missing 200 response for detail endpoint"
    assert "400" in detail_responses, "Missing 400 response for detail endpoint"
    assert "404" in detail_responses, "Missing 404 response for detail endpoint"

    # Schemas check
    schemas = spec["components"]["schemas"]
    expected_schemas = ["MaterialSummary", "MaterialDetail", "PaginationMeta", "MaterialSearchResult", "ErrorResponse"]
    for schema in expected_schemas:
        assert schema in schemas, f"Missing schema component '{schema}'"

    print("OpenAPI contract validation PASSED successfully!")

if __name__ == "__main__":
    contract_path = sys.argv[1] if len(sys.argv) > 1 else "docs/contracts/material-search.openapi.yaml"
    validate_openapi_contract(contract_path)
