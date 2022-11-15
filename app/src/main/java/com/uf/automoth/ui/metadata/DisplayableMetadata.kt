package com.uf.automoth.ui.metadata

import android.content.Context
import com.uf.automoth.R
import com.uf.automoth.utility.SHORT_DATE_TIME_FORMATTER
import java.time.OffsetDateTime

interface DisplayableMetadataInterface {
    val name: String
    val readonly: Boolean
    var deletable: Boolean
    suspend fun writeValue()

    // Should return null when the absence of a value is significant and should be displayed as "unknown"
    fun stringRepresentation(context: Context): String?
}

interface MetadataValueInterface<T> {
    var value: T?
    val setValue: suspend (T?) -> Unit
    val validate: (T?) -> Boolean
}

// Allows creating a List<Metadata> with heterogeneous contents in a type-safe manner
// This looks unnecessary, but it also allows polymorphism as opposed to template types that would
// be erased at runtime. Since there are relatively few types used, it seems like an okay compromise
// especially since it allows limiting the Metadata types to those that can actually be displayed
sealed class DisplayableMetadata : DisplayableMetadataInterface {
    override var deletable = false

    class StringMetadata(
        override val name: String,
        override val readonly: Boolean,
        override var value: String?,
        override val setValue: suspend (String?) -> Unit = {},
        override val validate: (String?) -> Boolean = { true }
    ) : DisplayableMetadata(), DisplayableMetadataInterface, MetadataValueInterface<String> {
        override suspend fun writeValue() {
            setValue(value)
        }

        override fun stringRepresentation(context: Context) = value
    }

    class IntMetadata(
        override val name: String,
        override val readonly: Boolean,
        override var value: Int?,
        override val setValue: suspend (Int?) -> Unit = {},
        override val validate: (Int?) -> Boolean = { true }
    ) : DisplayableMetadata(), DisplayableMetadataInterface, MetadataValueInterface<Int> {
        override suspend fun writeValue() {
            setValue(value)
        }

        override fun stringRepresentation(context: Context): String? = value?.toString()
    }

    class DoubleMetadata(
        override val name: String,
        override val readonly: Boolean,
        override var value: Double?,
        override val setValue: suspend (Double?) -> Unit = {},
        override val validate: (Double?) -> Boolean = { true }
    ) : DisplayableMetadata(), DisplayableMetadataInterface, MetadataValueInterface<Double> {
        override suspend fun writeValue() {
            setValue(value)
        }

        override fun stringRepresentation(context: Context): String? = value?.toString()
    }

    class BooleanMetadata(
        override val name: String,
        override val readonly: Boolean,
        var value: Boolean?,
        val setValue: suspend (Boolean?) -> Unit = {}
    ) : DisplayableMetadata(), DisplayableMetadataInterface {
        override suspend fun writeValue() {
            setValue(value)
        }

        override fun stringRepresentation(context: Context): String {
            return if (value == true) context.getString(R.string.yes) else context.getString(R.string.no)
        }
    }

    class DateMetadata(
        override val name: String,
        override val readonly: Boolean,
        override var value: OffsetDateTime?,
        override val setValue: suspend (OffsetDateTime?) -> Unit = {},
        override val validate: (OffsetDateTime?) -> Boolean = { true }
    ) : DisplayableMetadata(),
        DisplayableMetadataInterface,
        MetadataValueInterface<OffsetDateTime> {
        override suspend fun writeValue() {
            setValue(value)
        }

        override fun stringRepresentation(context: Context) =
            value?.format(SHORT_DATE_TIME_FORMATTER)
    }

    class Header(
        override val name: String,
        val showEditButton: Boolean = false
    ) : DisplayableMetadata() {
        override val readonly = true
        override suspend fun writeValue() {}
        override fun stringRepresentation(context: Context): String = ""
    }
}
