package eu.tib.orkg.prototype.util.upload

data class FileResponse(
    var file: String,
    var downloadUri: String,
    var fileType: String,
    var size: Long
)
