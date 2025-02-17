
{#
// Kotlin's `enum class` construct doesn't support variants with associated data,
// but is a little nicer for consumers than its `sealed class` enum pattern.
// So, we switch here, using `enum class` for enums with no associated data
// and `sealed class` for the general case.
#}

{%- if e.is_flat() %}

{%- call kt::docstring(e, 0) %}
{% match e.variant_discr_type() %}
{% when None %}
enum class {{ type_name }} {
    {% for variant in e.variants() -%}
    {%- call kt::docstring(variant, 4) %}
    {{ variant|variant_name }}{% if loop.last %};{% else %},{% endif %}
    {%- endfor %}
    companion object
}
{% when Some with (variant_discr_type) %}
enum class {{ type_name }}(val value: {{ variant_discr_type|type_name(ci) }}) {
    {% for variant in e.variants() -%}
    {%- call kt::docstring(variant, 4) %}
    {{ variant|variant_name }}({{ e|variant_discr_literal(loop.index0) }}){% if loop.last %};{% else %},{% endif %}
    {%- endfor %}
    companion object
}
{% endmatch %}
{% else %}

{%- call kt::docstring(e, 0) %}
sealed class {{ type_name }}{% if contains_object_references %}: Disposable {% endif %} {
    {% for variant in e.variants() -%}
    {%- call kt::docstring(variant, 4) %}
    {% if !variant.has_fields() -%}
    object {{ variant|variant_type_name(ci) }} : {{ type_name }}() {% if contains_object_references %} {
        override fun destroy() = Unit
    } {% endif %}
    {% else -%}
    data class {{ variant|variant_type_name(ci) }}(
        {%- for field in variant.fields() -%}
        {%- call kt::docstring(field, 8) %}
        val {% call kt::field_name(field, loop.index) %}: {{ field|type_name(ci) }},
        {%- endfor %}
    ) : {{ type_name }}() {
        {%- if contains_object_references %}
        override fun destroy() {
            {%- if variant.has_fields() -%}
            {%- call kt::destroy_fields(variant, 12) -%}
            {%- else %}
            // Nothing to destroy
            {%- endif %}
        }
        {%- endif %}
    }
    {%- endif %}
    {% endfor %}
}

{% endif %}
