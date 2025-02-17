
{#
// Template to call into rust. Used in several places.
// Variable names in `arg_list` should match up with arg lists
// passed to rust via `arg_list_lowered`
#}

{%- macro check_rust_buffer_length(length) -%}
    require({{ length }} <= Int.MAX_VALUE) {
        val length = {{ length }}
        "cannot handle RustBuffer longer than Int.MAX_VALUE bytes: length is $length"
    }
{%- endmacro %}

{%- macro to_ffi_call(func, indent) -%}
                        {%- if func.takes_self() -%}
                        callWithPointer {
{{ " "|repeat(indent) }}    {% call to_raw_ffi_call(func, indent + 4) %}
{{ " "|repeat(indent) }}{{ '}' }}
                        {%- else -%}
                        {%- call to_raw_ffi_call(func, indent) -%}
                        {%- endif -%}
{%- endmacro %}

{%- macro to_raw_ffi_call(func, indent) -%}
                        {%- match func.throws_type() -%}
                        {%- when Some with (e) -%}
                        uniffiRustCallWithError({{ e|type_name(ci) }}ErrorHandler)
                        {%- else -%}
                        uniffiRustCall
                        {%- endmatch %} { uniffiRustCallStatus ->
{{ " "|repeat(indent) }}    UniffiLib.INSTANCE.{{ func.ffi_func().name() }}(
                                {%- if func.takes_self() %}
{{ " "|repeat(indent) }}        it,
                                {%- endif -%}
                                {%- call arg_list_lowered(func, indent + 8) %}
{{ " "|repeat(indent) }}        uniffiRustCallStatus,
                        {%- if let Some(return_type) = func.ffi_func().return_type() -%}
                        {%-     if return_type|need_non_null_assertion %}
{{ " "|repeat(indent) }}    )!!
                        {%-     else %}
{{ " "|repeat(indent) }}    )
                        {%- endif -%}
                        {%- else %}
{{ " "|repeat(indent) }}    )
                        {%- endif %}
{{ " "|repeat(indent) }}{{ '}' }}
{%- endmacro -%}

{%- macro func_decl(func_decl, callable, indent, is_decl_override) %}
                        {%- call docstring(callable, indent) -%}
                        {%- match callable.throws_type() -%}
                        {%-     when Some(throwable) %}
{{ " "|repeat(indent) }}@Throws({{ throwable|type_name(ci) }}::class {%- if callable.is_async() -%}, kotlin.coroutines.cancellation.CancellationException::class{%- endif -%})
                        {%-     else -%}
                        {%- endmatch %}
{{ " "|repeat(indent) }}{% if func_decl.len() != 0 -%}{{ func_decl }} {% endif -%}
                        {%- if callable.is_async() -%}suspend {% endif -%}
                        fun {{ callable.name()|fn_name }}(
                            {%- call arg_list(callable, is_decl_override || !callable.takes_self()) -%}
                        )
                        {%- match callable.return_type() -%}
                        {%-     when Some with (return_type) %}: {{ return_type|type_name(ci) -}}
                        {%-     else -%}
                        {%- endmatch -%}
{% endmacro %}

{%- macro func_decl_with_body(func_decl, callable, indent) %}
                        {%- call docstring(callable, indent) -%}
                        {%- match callable.throws_type() -%}
                        {%-     when Some(throwable) %}
{{ " "|repeat(indent) }}@Throws({{ throwable|type_name(ci) }}::class {%- if callable.is_async() -%}, kotlin.coroutines.cancellation.CancellationException::class{%- endif -%})
                        {%-     else -%}
                        {%- endmatch %}
{{ " "|repeat(indent) }}{% if func_decl.len() != 0 -%}{{ func_decl }} {% endif -%}
                        {%- if callable.is_async() -%}suspend {% endif -%}
                        fun {{ callable.name()|fn_name }}(
                            {%- call arg_list(callable, false) -%}
                        )
                        {%- match callable.return_type() -%}
                        {%-     when Some with (return_type) %}: {{ return_type|type_name(ci) -}}
                        {%-     else -%}
                        {%- endmatch %} {
                            {%- if callable.is_async() %}
{{ " "|repeat(indent) }}    return {% call call_async(callable, indent + 4) -%}
                            {%- else -%}
                            {%- match callable.return_type() -%}
                            {%-     when Some with (return_type) %}
{{ " "|repeat(indent) }}    return {{ return_type|lift_fn }}({%- call to_ffi_call(callable, indent + 4) -%})
                            {%-     else %}
{{ " "|repeat(indent) }}    {% call to_ffi_call(callable, indent + 4) -%}
                            {%- endmatch %}
                            {%- endif %}
{{ " "|repeat(indent) }}{{ '}' }}
{% endmacro %}

{%- macro call_async(callable, indent) -%}
                        uniffiRustCallAsync(
                            {%- if callable.takes_self() %}
{{ " "|repeat(indent) }}    callWithPointer { thisPtr ->
{{ " "|repeat(indent) }}        UniffiLib.INSTANCE.{{ callable.ffi_func().name() }}(
{{ " "|repeat(indent) }}            thisPtr,
                                    {%- call arg_list_lowered(callable, indent + 12) %}
{{ " "|repeat(indent) }}        )
{{ " "|repeat(indent) }}    },
                            {%- else %}
{{ " "|repeat(indent) }}    UniffiLib.INSTANCE.{{ callable.ffi_func().name() }}(
                                {%- call arg_list_lowered(callable, indent + 8) %}
{{ " "|repeat(indent) }}    ),
                            {%- endif %}
{{ " "|repeat(indent) }}    {{ callable|async_poll(ci) }},
{{ " "|repeat(indent) }}    {{ callable|async_complete(ci) }},
{{ " "|repeat(indent) }}    {{ callable|async_free(ci) }},
{{ " "|repeat(indent) }}    {{ callable|async_cancel(ci) }},
{{ " "|repeat(indent) }}    // lift function
                            {%- match callable.return_type() -%}
                            {%- when Some(return_type) -%}
                            {%- if return_type|as_ffi_type|need_non_null_assertion %}
{{ " "|repeat(indent) }}    { {{ return_type|lift_fn }}(it!!) },
                            {%- else %}
{{ " "|repeat(indent) }}    { {{ return_type|lift_fn }}(it) },
                            {%- endif -%}
                            {%- when None %}
{{ " "|repeat(indent) }}    { Unit },
{{ " "|repeat(indent) }}    {% endmatch %}
{{ " "|repeat(indent) }}    // Error FFI converter
                            {%- match callable.throws_type() -%}
                            {%- when Some(e) %}
{{ " "|repeat(indent) }}    {{ e|type_name(ci) }}ErrorHandler,
                            {%- when None %}
{{ " "|repeat(indent) }}    UniffiNullRustCallStatusErrorHandler,
                            {%- endmatch %}
{{ " "|repeat(indent) }})
{%- endmacro %}

{%- macro arg_list_lowered(func, indent) -%}
                        {%- for arg in func.arguments() %}
{{ " "|repeat(indent) }}{{ arg|lower_fn }}({{ arg.name()|var_name }}),
                        {%- endfor -%}
{%- endmacro -%}

{#-
// Arglist as used in kotlin declarations of methods, functions and constructors.
// If is_decl, then default values be specified.
// Note the var_name and type_name filters.
-#}

{% macro arg_list(func, is_decl) %}
{%- for arg in func.arguments() -%}
        {{ arg.name()|var_name }}: {{ arg|type_name(ci) }}
{%-     if is_decl %}
{%-         match arg.default_value() %}
{%-             when Some with(literal) %} = {{ literal|render_literal(arg, ci) }}
{%-             else %}
{%-         endmatch %}
{%-     endif %}
{%-     if !loop.last %}, {% endif -%}
{%- endfor %}
{%- endmacro %}

{#-
// Arglist as used in the UniffiLib function declarations.
// Note unfiltered name but ffi_type_name filters.
-#}
{%- macro arg_list_ffi_decl(func, indent) -%}
                        {%- for arg in func.arguments() %}
{{ " "|repeat(indent) }}{{ arg.name()|var_name }}: {{ arg.type_().borrow()|ffi_type_name_by_value }},
                        {%- endfor -%}
                        {%- if func.has_rust_call_status_arg() %}
{{ " "|repeat(indent) }}uniffiCallStatus: UniffiRustCallStatus,
                        {%- endif -%}
{%- endmacro -%}

{%- macro arg_list_ffi_call_native(func) %}
    {%- for arg in func.arguments() -%}
        {%- if let Some(callback) = arg.type_().borrow()|ffi_as_callback(ci) -%}
        {%- if callback|ffi_callback_needs_casting_native %}
        {{ arg.name()|var_name }} as {{ci.namespace()}}.cinterop.{{ arg.type_().borrow()|ffi_type_name_for_ffi_struct }},
        {%- else %}
        {{ arg.name()|var_name }},
        {%- endif -%}
        {%- else %}
        {{ arg.name()|var_name }},
        {%- endif -%}
    {%- endfor -%}
    {%- if func.has_rust_call_status_arg() %}
        uniffiCallStatus,
    {%- endif -%}
{%- endmacro -%}

{% macro field_name(field, field_num) %}
{%- if field.name().is_empty() -%}
v{{- field_num -}}
{%- else -%}
{{ field.name()|var_name }}
{%- endif -%}
{%- endmacro %}

{% macro field_name_unquoted(field, field_num) %}
{%- if field.name().is_empty() -%}
v{{- field_num -}}
{%- else -%}
{{ field.name()|var_name|unquote }}
{%- endif -%}
{%- endmacro %}

 // Macro for destroying fields
{%- macro destroy_fields(member, indent) %}
{{ " "|repeat(indent) }}Disposable.destroy(
                            {%- for field in member.fields() %}
{{ " "|repeat(indent) }}    this.{%- call field_name(field, loop.index) -%},
                            {%- endfor %}
{{ " "|repeat(indent) }})
{%- endmacro -%}

{%- macro docstring_value(maybe_docstring, indent_spaces) %}
{%- match maybe_docstring %}
{%- when Some(docstring) %}
{{ docstring|docstring(indent_spaces) }}
{%- else %}
{%- endmatch %}
{%- endmacro %}

{%- macro docstring(defn, indent_spaces) %}
{%- call docstring_value(defn.docstring(), indent_spaces) %}
{%- endmacro %}
