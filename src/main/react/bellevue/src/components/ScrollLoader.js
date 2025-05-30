import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';

function ScrollLoader({
  children,
  loadMore,
  topLoad = false,
  total = 0,
  className
}) {
  const [loadingMore, setLoadingMore] = useState(false);
  const loadMoreRef = useRef(null);
  const containerRef = useRef(null);
  const prevScrollHeight = useRef(0);
  const debounceTimeout = useRef(null);

  useEffect(() => {
    // Reset loading state when children update
    setLoadingMore(false);
  }, [children?.length]);

  useEffect(() => {
    if (topLoad && containerRef.current) {
      const container = containerRef.current;
      const newScrollHeight = container.scrollHeight;
      const scrollDifference = newScrollHeight - prevScrollHeight.current;

      // Maintain scroll anchor when new items are prepended
      if (scrollDifference > 0) {
        container.scrollTop += scrollDifference;
      }

      prevScrollHeight.current = newScrollHeight;
    }
  }, [children?.length, topLoad]);

  useEffect(() => {
    if (topLoad && containerRef.current) {
      const container = containerRef.current;
      container.scrollTop = container.scrollHeight;
      prevScrollHeight.current = container.scrollHeight;
    }
  }, [topLoad]);

  useEffect(() => {
    const currentContainerRef = containerRef.current;
    const currentLoadMoreRef = loadMoreRef.current;
    if (!currentLoadMoreRef || !currentContainerRef || loadingMore || !children)
      return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !loadingMore) {
          if (debounceTimeout.current) clearTimeout(debounceTimeout.current);

          debounceTimeout.current = setTimeout(() => {
            setLoadingMore(true);
            prevScrollHeight.current = currentContainerRef.scrollHeight || 0;
            loadMore();
          }, 200);
        }
      },
      {
        root: currentContainerRef,
        rootMargin: '0px',
        threshold: 1.0
      }
    );

    observer.observe(currentLoadMoreRef);

    return () => {
      observer.disconnect();
      if (debounceTimeout.current) clearTimeout(debounceTimeout.current);
    };
  }, [children, loadingMore, loadMore]);

  const loadMoreWrapper = () => {
    if (loadingMore) return;
    setLoadingMore(true);
    prevScrollHeight.current = containerRef.current?.scrollHeight || 0;
    loadMore();
  };

  return (
    <div className={`scroll-loader ${className}`} ref={containerRef}>
      {topLoad &&
        total > (children ? children.length : 0) &&
        (loadingMore ? (
          <p>Loading...</p>
        ) : (
          <button ref={loadMoreRef} onClick={loadMoreWrapper}>
            Load more
          </button>
        ))}
      {children}
      {!topLoad &&
        total > (children ? children.length : 0) &&
        (loadingMore ? (
          <p>Loading...</p>
        ) : (
          <button ref={loadMoreRef} onClick={loadMoreWrapper}>
            Load more
          </button>
        ))}
    </div>
  );
}

ScrollLoader.propTypes = {
  children: PropTypes.node,
  loadMore: PropTypes.func.isRequired,
  topLoad: PropTypes.bool,
  total: PropTypes.number,
  className: PropTypes.string
};

ScrollLoader.displayName = 'ScrollLoader';

export default withAuth(ScrollLoader);
