'use client'

import { useState } from 'react'
import { Button } from "@/components/ui/button"
import { Editor } from '@tinymce/tinymce-react'

interface UpdateEditorProps {
  onSend: (content: string) => void;
}

export function UpdateEditor({ onSend }: UpdateEditorProps) {
  const [content, setContent] = useState('')

  const handleEditorChange = (content: string) => {
    setContent(content)
  }

  const handleSend = () => {
    onSend(content)
    setContent('')
  }

  return (
    <div className="space-y-4">
      <Editor
        apiKey="your-tinymce-api-key" // Replace with your actual TinyMCE API key
        init={{
          height: 300,
          menubar: false,
          plugins: [
            'advlist autolink lists link image charmap print preview anchor',
            'searchreplace visualblocks code fullscreen',
            'insertdatetime media table paste code help wordcount'
          ],
          toolbar: 'undo redo | formatselect | ' +
            'bold italic backcolor | alignleft aligncenter ' +
            'alignright alignjustify | bullist numlist outdent indent | ' +
            'removeformat | help',
        }}
        value={content}
        onEditorChange={handleEditorChange}
      />
      <Button onClick={handleSend}>Send Update</Button>
    </div>
  )
}

