
{%- let rec = ci|get_record_definition(name) %}

{%- if rec.has_fields() %}
{%- call kt::docstring(rec, 0) %}
data class {{ type_name }} (
    {%- for field in rec.fields() %}
    {%- call kt::docstring(field, 4) %}
    {% if config.generate_immutable_records() %}val{% else %}var{% endif %} {{ field.name()|var_name }}: {{ field|type_name(ci) -}}
    {%- match field.default_value() %}
        {%- when Some with(literal) %} = {{ literal|render_literal(field, ci) }}
        {%- else %}
    {%- endmatch -%}
    {% if !loop.last %}, {% endif %}
    {%- endfor %}
) {% if contains_object_references %}: Disposable {% endif %}{
    {% if contains_object_references %}
    override fun destroy() {
        {%- call kt::destroy_fields(rec, 8) %}
    }
    {% endif %}
    companion object
}
{%- else -%}
{%- call kt::docstring(rec, 0) %}
class {{ type_name }} {
    override fun equals(other: Any?): Boolean {
        return other is {{ type_name }}
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object
}
{%- endif %}
