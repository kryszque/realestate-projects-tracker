package com.mcdevka.realestate_projects_tracker.domain.tag;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {return tagRepository.findAll();}

    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag with ID " + id + " not found!"));
    }

    public Tag createTag(Tag inputTag) {
        if (tagRepository.existsByName(inputTag.getName())) {
            throw new IllegalArgumentException("Tag with name " + inputTag.getName() + " already exists!");
        }

        Tag createdTag = new Tag();
        createdTag.setName(inputTag.getName());
        createdTag.setColor(inputTag.getColor());

        return tagRepository.save(createdTag);
    }

    public Tag updateTag(Long id, Tag inputTag) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        tag.setName(inputTag.getName());
        tag.setColor(inputTag.getColor());

        return tagRepository.save(tag);
    }

    public Tag archiveTag(Long id){
        Tag archivedTag = getTagById(id);
        archivedTag.setState("archived");
        return tagRepository.save(archivedTag);
    }
}
