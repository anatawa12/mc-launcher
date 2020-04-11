package com.anatawa12.mcLauncher.launchInfo.json.adapters

import com.squareup.moshi.*
import java.lang.reflect.Array
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * if the element starts with '[', the element will be parsed as collection.
 * if not, the element will be parsed as single element of the collection.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class NonArrayIfSingle(
    /**
     * if true, the 'null' will be a element of collection.
     * if not, the 'null' will be 'null' as collection.
     */
    val nullAsElement: Boolean = false
)

object NonArrayIfSingleAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val nonArrayIfSingle = annotations
            .filterIsInstance<NonArrayIfSingle>()
            .singleOrNull() ?: return null
        val otherAnnotations = annotations.filterNotTo(mutableSetOf()) { it is NonArrayIfSingle }

        val rawType = Types.getRawType(type)

        require(rawType.isArray || rawType == List::class.java || rawType == Collection::class.java || rawType == Set::class.java) {
            "the type is not array, List<>, Collection<>, or Set."
        }

        val nextAdapter = moshi.nextAdapter<Any>(this, type, otherAnnotations)

        if (rawType.isArray) {
            val elementType = getArrayComponentType(type)

            val elementAdapter = moshi.adapter<Any?>(elementType)
            val elementRawType = rawType.componentType
            return ArrayAdapter(
                nonArrayIfSingle.nullAsElement,
                castAdapter(nextAdapter),
                elementAdapter,
                elementRawType
            )
        }

        val elementAdapter = moshi.adapter<Any?>(getElementType(type))

        if (rawType == Set::class.java) {
            return SetAdapter(
                nonArrayIfSingle.nullAsElement,
                elementAdapter,
                castAdapter(nextAdapter)
            )
        } else {
            return ListAdapter(
                nonArrayIfSingle.nullAsElement,
                elementAdapter,
                castAdapter(nextAdapter)
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> castAdapter(adapter: JsonAdapter<*>): JsonAdapter<T> = adapter as JsonAdapter<T>

    fun getArrayComponentType(type: Type?): Type {
        return when (type) {
            is GenericArrayType -> type.genericComponentType!!
            is Class<*> -> type.componentType!!
            else -> error("type is not array")
        }
    }

    private fun getElementType(collectionType: Type): Type {
        if (collectionType is ParameterizedType)
            return collectionType.actualTypeArguments[0]
        else
            return Any::class.java
    }

    private class ListAdapter<E : Any>(
        nullAsElement: Boolean,
        element: JsonAdapter<E?>,
        collection: JsonAdapter<List<E?>>
    ) : CollectionAdapter<List<E?>, E>(nullAsElement, collection, element) {
        override fun maker(single: E?): List<E?> = mutableListOf()
    }

    private class SetAdapter<E : Any>(
        nullAsElement: Boolean,
        element: JsonAdapter<E?>,
        collection: JsonAdapter<Set<E?>>
    ) : CollectionAdapter<Set<E?>, E>(nullAsElement, collection, element) {
        override fun maker(single: E?): Set<E?> = mutableSetOf()
    }

    private abstract class CollectionAdapter<C : Collection<E?>, E : Any>(
        nullAsElement: Boolean,
        collection: JsonAdapter<C>,
        element: JsonAdapter<E?>
    ) : Adapter<C, E?>(nullAsElement, collection, element) {
        fun getIsSingleAndValueInternal(array: Collection<E?>): Pair<Boolean, E?> {
            when (array) {
                is List -> return if (array.size == 1) true to array[0] else false to null
                else -> {
                    val iterator = array.iterator()
                    if (!iterator.hasNext())
                        return false to null
                    val single = iterator.next()
                    if (iterator.hasNext())
                        return false to null
                    return true to single
                }
            }
        }

        override fun getIsSingleAndValue(array: C): Pair<Boolean, E?> = getIsSingleAndValueInternal(array)
    }

    private class ArrayAdapter(
        nullAsElement: Boolean,
        array: JsonAdapter<Any>,
        element: JsonAdapter<Any?>,
        val elementRawType: Class<*>
    ) : Adapter<Any, Any?>(nullAsElement, array, element) {
        override fun maker(single: Any?): Any {
            val array = Array.newInstance(elementRawType, 1)
            Array.set(array, 0, single)
            return array
        }

        override fun getIsSingleAndValue(array: Any): Pair<Boolean, Any?> {
            val length = Array.getLength(this)
            if (length == 1)
                return true to Array.get(this, 0)
            return false to null
        }
    }

    abstract class Adapter<TArray, TElement>(
        private val nullAsElement: Boolean,
        private val array: JsonAdapter<TArray>,
        private val element: JsonAdapter<TElement>
    ) : JsonAdapter<TArray>() {
        protected abstract fun maker(single: TElement?): TArray
        protected abstract fun getIsSingleAndValue(array: TArray): Pair<Boolean, TElement>

        final override fun fromJson(reader: JsonReader): TArray? {
            return when (reader.peek()) {
                JsonReader.Token.NULL -> if (nullAsElement) maker(null) else null
                JsonReader.Token.BEGIN_ARRAY -> array.fromJson(reader)
                else -> maker(element.fromJson(reader))
            }
        }

        final override fun toJson(writer: JsonWriter, value: TArray?) {
            if (value == null) {
                if (nullAsElement)
                    throw JsonDataException("null collection is not allowed for NonArrayIfSingle(nullAsElement=true)")
                writer.nullValue()
                return
            }

            val (isSingle, single) = getIsSingleAndValue(value)

            if (isSingle && (nullAsElement || single != null)) {
                element.toJson(writer, single)
            } else {
                array.toJson(writer, value)
            }
        }
    }
}
